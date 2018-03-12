package org.mib.robot.input;

import org.mib.robot.event.Event;

public class JoystickEvent extends Event {
   public enum Type {
      Button,
      Axis
   }

   private final Type type;
   private final int index;
   private final float value;
   private boolean pressed;

   JoystickEvent(int index, float value) {
      this.index = index;
      this.type = Type.Axis;
      this.value = value;
   }

   JoystickEvent(int index, boolean pressed) {
      this.index = index;
      this.type = Type.Button;
      this.pressed = pressed;
      this.value = pressed ? 1f : 0f;
   }

   public Type getType() {
      return type;
   }

   public int getIndex() {
      return index;
   }

   public float getValue() {
      return value;
   }

   public boolean isPressed() {
      return pressed;
   }
}
