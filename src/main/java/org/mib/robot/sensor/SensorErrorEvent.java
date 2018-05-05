package org.mib.robot.sensor;

import org.mib.robot.event.Event;

@SuppressWarnings("WeakerAccess")
public class SensorErrorEvent extends Event {
   private final String source;

   public SensorErrorEvent(String source) {
      this.source = source;
   }

   @SuppressWarnings("unused")
   public String getSource() {
      return source;
   }
}
