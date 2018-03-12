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
         latch.await(60, TimeUnit.SECONDS);
      }

      private List<Event> getReadEvents() {
         return readEvents;
      }
   }

   @Test(expected = NoSuchFileException.class)
   public void testBadOpen() throws IOException {
      Joystick joystick = new Joystick("notafile", null);
      joystick.open();
   }

   @Test
   public void open() throws IOException {
      try(Joystick joystick = new Joystick(singleTempFile.toString(), null)) {
         joystick.open();
         Assert.assertTrue("Joystick not running", joystick.isRunning());
      }
   }

   @Test
   public void testProcessEvents() throws IOException, InterruptedException {
      testProcessEvents(1, null, singleTempFile);
      testProcessEvents(3, null, multipleTempFile);
      testProcessEvents(2, new int[] { 7 }, multipleTempFile);
      testProcessEvents(3, new int[] { 7, 8 }, multipleTempFile);
   }

   private void testProcessEvents(int numberOfEvents, int[] axes, Path testFile) throws IOException,
         InterruptedException {
      TestHandler singleHandler = new TestHandler(numberOfEvents);
      try(Joystick joystick = new Joystick(testFile.toString(), axes)) {
         joystick.addHandler(singleHandler);
         joystick.open();
         singleHandler.await();
      }
      Assert.assertEquals("Not all events read", numberOfEvents, singleHandler.getReadEvents()
            .size());
   }

   @Test
   public void close() throws IOException {
      try(Joystick joystick = new Joystick(singleTempFile.toString(), null)) {
         joystick.open();
      }
   }

}