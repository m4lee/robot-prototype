package org.mib.robot.motor;

import com.pi4j.io.gpio.*;
import org.mib.robot.pi.GpioUtil;

import javax.inject.Inject;

/**
 * A facade to the GPIO interface to control the motors. This controls a DFRobot MD1.3.
 *
 * @see
 * <a href="https://www.dfrobot.com/wiki/index.php/MD1.3_2A_Dual_Motor_Controller_SKU_DRI0002">Motor
 * Controller Wiki</a>
 */
public class Motor {
   private static final int MAX_MOTORS = 16;
   private final MotorInstance[] instances = new MotorInstance[MAX_MOTORS];

   private static class MotorInstance {
      @SuppressWarnings("unused")
      private final String name;
      private final GpioPinPwmOutput enablePin;
      private final GpioPinDigitalOutput directionPin;
      private final boolean invert;
      private final int range;
      private final float minPwm;

      private MotorInstance(String name, GpioPinPwmOutput enablePin, GpioPinDigitalOutput
            directionPin, boolean invert, int range, float minPwm) {
         this.name = name;
         this.enablePin = enablePin;
         this.directionPin = directionPin;
         this.invert = invert;
         this.range = range;
         this.minPwm = minPwm;
      }

      private GpioPinPwmOutput getEnablePin() {
         return enablePin;
      }

      private GpioPinDigitalOutput getDirectionPin() {
         return directionPin;
      }

      private boolean isInvert() {
         return invert;
      }

      private int translateSpeed(float speed) {
         assert -1f <= speed && speed <= 1f;
         // we can't use the really low values since the motor won't always start at the minimum
         // value. Map the given speed to a workable speed for the motor.
         float absoluteSpeed = Math.abs(speed);
         float translatedSpeed = absoluteSpeed > 0 ? minPwm + (0.95f - minPwm) * absoluteSpeed : 0;
         return Math.round(translatedSpeed * range);
      }
   }

   @Inject
   @SuppressWarnings("WeakerAccess")
   GpioController gpio;

   @Inject Motor() {
      // allow injection
   }

   void registerInstance(int index, String name, int enableNumber, int directionNumber,
                         boolean invert, int pwmRange, float minPwm) {
      assert 0 <= index && index < MAX_MOTORS;
      assert name != null;
      assert 0 <= enableNumber && enableNumber < 32;
      assert 0 <= directionNumber && directionNumber < 32;

      GpioPinPwmOutput enablePin = gpio.provisionPwmOutputPin(GpioUtil.toPin(enableNumber),
            name + " E1", 0);
      enablePin.setShutdownOptions(true, PinState.LOW);
      enablePin.setPwmRange(pwmRange);

      GpioPinDigitalOutput directionPin = gpio.provisionDigitalOutputPin(GpioUtil.toPin(
            directionNumber), name + "M1", PinState.LOW);
      directionPin.setShutdownOptions(true, PinState.LOW);

      instances[index] = new MotorInstance(name, enablePin, directionPin, invert, pwmRange, minPwm);
   }

   public void setSpeed(int index, float speed) {
      MotorInstance instance = instances[index];
      PinState direction = speed >= 0 && !instance.isInvert() ||
            speed < 0 && instance.isInvert()
            ? PinState.LOW : PinState.HIGH;

      // the motor can be flipped on one side so we have to invert the direction
      instance.getDirectionPin().setState(direction);
      instance.getEnablePin().setPwm(instance.translateSpeed(speed));
   }
}
