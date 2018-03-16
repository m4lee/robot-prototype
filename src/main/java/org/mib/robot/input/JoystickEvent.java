package org.mib.robot.input;

import org.mib.robot.event.Event;

/**
 * Raised when a change occurs on a joystick.
 */
public class JoystickEvent extends Event {
   public enum Type {
      Button,
      Axis
   }

   private final Type type;
   private final int index;
   private final float value;
   private boolean pressed;

   public JoystickEvent(int index, float value) {
      this.index = index;
      this.type = Type.Axis;
      this.value = value;
   }

   @SuppressWarnings("WeakerAccess")
   public JoystickEvent(int index, boolean pressed) {
      this.index = index;
      this.type = Type.Button;
      this.pressed = pressed;
      this.value = pressed ? 1f : 0f;
   }

   /**
    * Specifies if the position of a stick or a button press occurred.
    * @return an instance of a {@link Type} indicating how the joystick has manipulated.
    */
   public Type getType() {
      return type;
   }

   /**
    * Specifies which joystick axis or button was updated.
    * @return An identifier of which axis or button was updated.
    */
   public int getIndex() {
      return index;
   }

   /**
    * Specifies the position of a joystick axis. The value varies between -1.0 and 1.0, inclusive.
    * @return the position of a joystick axis.
    * @apiNote This will return a non-zero value if a button was pressed, zero if the button was not
    * pressed. Also, try {@link #isPressed()}
    */
   public float getValue() {
      return value;
   }

   /**
    * Return if a button was pressed.
    * @return true if the button was pressed, false if it was released.
    */
   public boolean isPressed() {
      return pressed;
   }

   @Override
   public String toString() {
      return "JoystickEvent (timestamp: " + getTimestamp() +
            ", type: " + type +
            ", index: " + index +
            ", value: " + value +
            ", pressed: " + pressed + ")";
   }
}
