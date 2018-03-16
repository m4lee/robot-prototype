package org.mib.robot.motor;

import org.mib.robot.event.Event;

public class ChangeMotorSpeedEvent extends Event {
   private final int motor;
   private final float speed;

   public ChangeMotorSpeedEvent(int motor, float speed) {
      this.motor = motor;
      this.speed = speed;
   }

   public int getMotor() {
      return motor;
   }

   public float getSpeed() {
      return speed;
   }

   public String toString() {
      return "ChangeMotorSpeedEvent (timestamp: " + getTimestamp() +
            ", motor: " + motor +
            ", speed: " + speed + ")";
   }
}
