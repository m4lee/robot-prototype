package org.mib.robot.motor;

import com.pi4j.io.gpio.*;
import org.mib.robot.pi.GpioUtil;

import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * A facade to the GPIO interface to control the motors. This controls a DFRobot MD1.3.

 * The pin assignments are below:
 * <table>
 *    <tr>
 *       <td>Left</td> <td>E1</td> <td>PWM Speed</td> <td>RaspiPin.GPIO_24</td>
 *    </tr>
 *    <tr>
 *       <td>Left</td> <td>M1</td> <td>Direction</td> <td>RaspiPin.GPIO_23</td>
 *    </tr>
 *    <tr>
 *       <td>Right</td> <td>E2</td> <td>PWM Speed</td> <td>RaspiPin.GPIO_26</td>
 *    </tr>
 *    <tr>
 *       <td>Right</td> <td>M2</td> <td>Direction</td> <td>RaspiPin.GPIO_27</td>
 *    </tr>
 * </table>
 *
 * @see
 * <a href="https://www.dfrobot.com/wiki/index.php/MD1.3_2A_Dual_Motor_Controller_SKU_DRI0002">Motor
 * Controller Wiki</a>
 */
public class Motor {
   private static final int MAX_MOTORS = 16;
   private final MotorInstance[] instances = new MotorInstance[MAX_MOTORS];

   private static class MotorInstance {
      private final String name;
      private final GpioPinPwmOutput enablePin;
      private final GpioPinDigitalOutput directionPin;
      private final boolean invert;

      private MotorInstance(String name, GpioPinPwmOutput enablePin, GpioPinDigitalOutput
            directionPin, boolean invert) {
         this.name = name;
         this.enablePin = enablePin;
         this.directionPin = directionPin;
         this.invert = invert;
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
   }

   /**
    * The number of cycles in one complete PWM cycle. We're assuming a clock of 19.2Mhz with a
    * clock divider of 8. This gives a final frequency of 2.34 kHz.
    *
    * @see org.mib.robot.pi.GpioModule#PWM_CLOCK_FREQUENCY
    * @see org.mib.robot.pi.GpioModule#CLOCK_DIVISOR
    */
   private static final int TICKS = 1024;

   @Inject
   @SuppressWarnings("WeakerAccess")
   GpioController gpio;

   private GpioPinPwmOutput leftEnablePin;
   private GpioPinDigitalOutput leftDirectionPin;
   private GpioPinPwmOutput rightEnablePin;
   private GpioPinDigitalOutput rightDirectionPin;

   @Inject Motor() {
      // allow injection
   }

   void registerInstance(int index, String name, int enableNumber, int directionNumber,
                         boolean invert, int pwmRange) {
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

      instances[index] = new MotorInstance(name, enablePin, directionPin, invert);
   }

   public void setSpeed(int index, float speed) {
      PinState direction = speed >= 0 && !instances[index].isInvert() ||
            speed < 0 && instances[index].isInvert()
            ? PinState.LOW : PinState.HIGH;

      // the motor can be flipped on one side so we have to invert the direction
      instances[index].getDirectionPin().setState(direction);
      instances[index].getEnablePin().setPwm(translateSpeed(speed));
   }

   private int translateSpeed(float speed) {
      assert speed >= -1.0f && speed <= 1.0f;

      float absoluteSpeed = Math.abs(speed);
      float maxValue = (float)TICKS * 0.95f;
      return Math.round(absoluteSpeed * maxValue);
   }

}
