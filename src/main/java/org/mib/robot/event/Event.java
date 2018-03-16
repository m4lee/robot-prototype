package org.mib.robot.event;

public class Event {
   private final long timestamp;

   protected Event() {
      this.timestamp = System.nanoTime();
   }

   @SuppressWarnings("unused")
   public Event(long timestamp) {
      this.timestamp = timestamp;
   }

   public long getTimestamp() {
      return timestamp;
   }
}
