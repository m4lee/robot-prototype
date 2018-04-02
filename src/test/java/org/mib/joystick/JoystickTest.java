package org.mib.joystick;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class JoystickTest {


   private static final Path singleTempFile = Paths.get
         ("src/test/resources/org/mib/joystick/test-single-event.bin");
   private static final Path multipleTempFile = Paths.get
         ("src/test/resources/org/mib/joystick/test-multiple-events.bin");

   private static class TestHandler implements Consumer<Event> {
      private final List<Event> readEvents = new ArrayList<>();
      private final CountDownLatch latch;

      private TestHandler(int expectedEvents) {
         latch = new CountDownLatch(expectedEvents);
      }

      @Override
      public void accept(Event event) {
         readEvents.add(event);
         latch.countDown();
      }

      private void await() throws InterruptedException {
         latch.await(20, TimeUnit.SECONDS);
      }

      private List<Event> getReadEvents() {
         return readEvents;
      }
   }

   @Test(expected = NoSuchFileException.class)
   public void testBadOpen() throws NoSuchFileException {
      Joystick joystick = new Joystick("notadir/notafile");
      joystick.open();
   }

   @Test
   public void testOpen() throws NoSuchFileException {
      try(Joystick joystick = new Joystick(singleTempFile.toString())) {
         joystick.open();
         Assert.assertTrue("Joystick not running", joystick.isRunning());
      }
   }

   @Test
   public void testWaitOpen() throws IOException, InterruptedException {
      Path tempDirectory = Files.createTempDirectory("JoystickTest");
      tempDirectory.toFile().deleteOnExit();

      // define where the new file will exist
      Path newFile = tempDirectory.resolve(singleTempFile.getFileName());
      newFile.toFile().deleteOnExit();

      TestHandler handler = new TestHandler(1);
      try(Joystick joystick = new Joystick(newFile.toString())) {
         joystick.addHandler(handler);
         joystick.open();

         // don't create the file immediately
         Thread.sleep(500);

         // Add the new file
         Files.copy(singleTempFile, newFile);

         // joystick should be waiting for file to be added
         handler.await();
      }
      assertEquals("No events captured", 1, handler.getReadEvents().size());
   }

   @Test
   public void testRaiseEvent() {
      TestHandler handler = new TestHandler(1);
      Event event = new Event(0, 0, Event.Type.BUTTON, 0, false);
      try(Joystick joystick = new Joystick("notafile")) {
         joystick.addHandler(handler);
         joystick.raiseEvent(event);
      }
      assertEquals("No events captured", 1, handler.getReadEvents().size());
      assertEquals("Event not captured", event, handler.getReadEvents().get(0));
   }

   @Test
   public void testProcessEvents() throws InterruptedException, NoSuchFileException {
      testProcessEvents(1, null, singleTempFile);
      testProcessEvents(3, null, multipleTempFile);
      testProcessEvents(2, new int[] { 7 }, multipleTempFile);
      testProcessEvents(3, new int[] { 7, 8 }, multipleTempFile);
   }

   private void testProcessEvents(int numberOfEvents, int[] axes, Path testFile)
         throws InterruptedException, NoSuchFileException {
      TestHandler singleHandler = new TestHandler(numberOfEvents);
      try(Joystick joystick = new Joystick(testFile.toString())) {
         joystick.addHandler(singleHandler);
         joystick.setAxes(axes);
         joystick.open();
         singleHandler.await();
      }
      assertEquals("Not all events read", numberOfEvents, singleHandler.getReadEvents()
            .size());
   }

   @Test
   public void testClose() throws NoSuchFileException {
      try(Joystick joystick = new Joystick(singleTempFile.toString())) {
         joystick.open();
      }
   }

}