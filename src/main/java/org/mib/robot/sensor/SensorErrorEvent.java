package org.mib.robot.sensor;

import org.mib.robot.event.Event;

@SuppressWarnings("WeakerAccess")
public class SensorErrorEvent extends Event {
   private final Object source;

   public SensorErrorEvent(Object source) {
      this.source = source;
   }

   @SuppressWarnings("unused")
   public Object getSource() {
      return source;
   }
}
