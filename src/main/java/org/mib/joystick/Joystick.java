package org.mib.joystick;

import com.google.common.annotations.VisibleForTesting;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
      private final Logger log = Logger.getLogger(EventReader.class.getName());
      private final ByteChannel characterDevice;
      private volatile boolean stop = false;

      private static final int EVENT_BUFFER_SIZE = 10;

      private EventReader(ByteChannel characterDevice) {
         this.characterDevice = characterDevice;
      }

      @Override
      public void run() {
         try {
            ByteBuffer buffer = ByteBuffer.allocate(Event.SIZE * EVENT_BUFFER_SIZE); // read ten events at a time
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            while (!stop) {
               int bytesRead = processEvents(buffer);
               if (bytesRead == -1) {
                  log.warning("Read the end of the joystick character device. Stopping event " +
                        "processing.");
                  stop = true;
               }
               if (bytesRead % Event.SIZE == 0) {
                  buffer.clear();
               }
            }
         } catch(ClosedByInterruptException cbie) {
            log.fine("Reading joystick events cancelled.");
         } catch(Exception e) {
            log.log(Level.SEVERE, "Error while accepting joystick input.", e);
         } finally {
            if(characterDevice != null) {
               try {
                  characterDevice.close();
               } catch(IOException ioe) {
                  log.log(Level.WARNING, "Error closing joystick device, " +
                        characterDevice, ioe);
               }
            }
         }

         log.info("Stopped reading joystick events.");
      }

      private int processEvents(ByteBuffer buffer) throws IOException {
         int bytesRead = characterDevice.read(buffer);
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

   public void open() throws IOException {
      log.info("Reading joystick events from " + devicePath + ".");
      if(eventReaderFuture == null) {
         eventReader = new EventReader(FileChannel.open(devicePath, StandardOpenOption.READ));
         eventReaderFuture = executor.submit(eventReader);
      } else {
         throw new IllegalStateException("Joystick processing has already been started.");
      }
   }

   public boolean isRunning() {
      return eventReaderFuture != null && !eventReaderFuture.isDone();
   }

   @Override
   public void close() {
      if(eventReader != null) {
         eventReader.stop();
      }

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
