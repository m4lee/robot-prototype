package org.mib.joystick;

import com.google.common.annotations.VisibleForTesting;
import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

/**
 * Wrapper over linux joystick API.
 * @see <a href="https://www.kernel.org/doc/Documentation/input/joystick-api.txt">Joystick API
 * Documentation</a>
 */
public class Joystick implements Closeable, AutoCloseable {
   private static final Logger log = Logger.getLogger(Joystick.class.getName());
   private static final int MAX_AXIS = 256;

   private final Path devicePath;

   private EventReader eventReader;
   private final ArrayList<Consumer<Event>> handlers = new ArrayList<>();
   private volatile boolean[] axes = new boolean[MAX_AXIS];

   private Future<?> eventReaderFuture;
   private final ExecutorService executor = Executors.newSingleThreadExecutor();

   private class EventReader implements Runnable {
      private static final int EVENT_BUFFER_SIZE = 10;

      private final Logger log = Logger.getLogger(EventReader.class.getName());
      private volatile boolean stop = false;

      @Override
      public void run() {
         try {
            ByteBuffer buffer = ByteBuffer.allocate(Event.SIZE * EVENT_BUFFER_SIZE); // read ten events at a time
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            while (!stop) {
               try (ByteChannel eventChannel = openFileDevice(devicePath)) {
                  log.info("Joystick at " + devicePath + " was connected.");
                  buffer.clear();
                  while (!stop) {
                     int bytesRead = readAndRaiseEvents(eventChannel, buffer);
                     if (bytesRead == -1) {
                        log.warning("Read the end of the joystick character device. Stopping event " +
                              "processing.");
                        stop = true;
                     }
                     if (bytesRead % Event.SIZE == 0) {
                        buffer.clear();
                     }
                  }
               } catch(IOException ioe) {
                  // check to see if the file has been remoed
                  if(ioe instanceof NoSuchFileException || !devicePath.toFile().exists()) {
                     log.info("Joystick at " + devicePath + " was disconnected.");
                  } else {
                     // this is unexpected so rethrow
                     throw ioe;
                  }
               }
            }
         } catch(ClosedByInterruptException | InterruptedException cbie) {
            log.fine("Reading joystick events cancelled.");
         } catch(Exception e) {
            log.log(Level.SEVERE, "Error while accepting joystick input.", e);
         }

         log.info("Stopped reading joystick events.");
      }

      private ByteChannel openFileDevice(Path path) throws IOException, InterruptedException {
         try(WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.getParent().register(watchService, new WatchEvent.Kind[]{ ENTRY_CREATE,
                  ENTRY_DELETE }, SensitivityWatchEventModifier.MEDIUM);
            while(!path.toFile().exists()) {
                  log.info("Waiting to connect to joystick at " + path.toString() + ".");
                  WatchKey watchKey = watchService.take();
                  boolean foundEvent = false;
                  for (WatchEvent event : watchKey.pollEvents()) {
                     if(ENTRY_CREATE == event.kind() &&
                           ((Path)event.context()).getFileName().equals(path.getFileName())) {
                        foundEvent = true;
                        break;
                     }
                  }
                  if(foundEvent) {
                     break;
                  }
               }
            }


         return FileChannel.open(path, StandardOpenOption.READ);
      }

      private int readAndRaiseEvents(ByteChannel eventChannel, ByteBuffer buffer) throws IOException {
         int bytesRead = eventChannel.read(buffer);
         buffer.flip();
         for(int i = 0; i < bytesRead / Event.SIZE; i++) {
            raiseEvent(new Event(buffer));
         }
         return bytesRead;
      }

      private void stop() {
         stop = true;
      }

   }

   /**
    * Construct an instance of the wrapper. One instance is required for each joystick.
    *
    * @param devicePath the path to the joystick character device. e.g. /dev/input/js0
    */
   public Joystick(String devicePath) {
      assert devicePath != null;

      this.devicePath = Paths.get(devicePath);
      this.eventReader = new EventReader();
   }

   public void addHandler(Consumer<Event> handler) {
      this.handlers.add(handler);
   }

   public void setAxes(int[] axes) {
      assert axes == null || axes.length <= MAX_AXIS;
      boolean[] tmp = new boolean[MAX_AXIS];
      if(axes != null) {
         for (int axe : axes) {
            tmp[axe] = true;
         }
      } else {
         for(int i = 0; i < this.axes.length; i++) {
            tmp[i] = true;
         }
      }
      this.axes = tmp;
   }

   @VisibleForTesting
   public void raiseEvent(Event evt) {
      if (evt.getEventType() == Event.Type.BUTTON || axes[evt.getAxisOrButton()]) {
         handlers.forEach(h -> {
            if (h != null) {
               try {
                  h.accept(evt);
               } catch (Exception e) {
                  log.log(Level.SEVERE, "Error handling joystick event.", e);
               }
            }
         });
      }
   }

   public void open() throws NoSuchFileException {
      if(devicePath.getParent() != null && !devicePath.getParent().toFile().exists()) {
         throw new NoSuchFileException(devicePath.getParent().toString());
      }

      log.info("Reading joystick events from " + devicePath + ".");
      if(eventReaderFuture == null) {
         eventReaderFuture = executor.submit(eventReader);
      } else {
         throw new IllegalStateException("Joystick processing has already been started.");
      }
   }

   boolean isRunning() {
      return eventReaderFuture != null && !eventReaderFuture.isDone();
   }

   @Override
   public void close() {
      eventReader.stop();

      try {
         if (eventReaderFuture != null) {
            eventReaderFuture.cancel(true);
         }
      } catch(Exception e) {
         log.log(Level.WARNING, "Unable to cancel joystick read task. Complete shutdown may " +
               "fail.", e);
      }

      executor.shutdown();

      try {
         executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
      } catch(InterruptedException ie) {
         log.log(Level.INFO, "Interrupted before completely shutting down joystick read task.");
      }
   }
}
