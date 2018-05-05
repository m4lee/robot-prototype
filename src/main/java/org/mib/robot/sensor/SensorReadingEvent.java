package org.mib.robot.sensor;

import org.mib.robot.event.Event;

public class SensorReadingEvent extends Event {
   private final String source;
   private final float reading;

   SensorReadingEvent(String source, float reading, long time) {
      super(time);
      this.source = source;
      this.reading = reading;
   }

   public String getSource() {
      return source;
   }

   public float getReading() {
      return reading;
   }
}
