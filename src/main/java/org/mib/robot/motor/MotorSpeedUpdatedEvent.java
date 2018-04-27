package org.mib.robot.motor;

import org.mib.robot.event.Event;

public class MotorSpeedUpdatedEvent extends Event {
   private final int motor;

   public MotorSpeedUpdatedEvent(int motor) {
      this.motor = motor;
   }

   public int getMotor() {
      return motor;
   }
}
