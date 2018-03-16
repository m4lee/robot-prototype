package org.mib.robot.motor;

import com.pi4j.io.gpio.*;

import javax.inject.Inject;

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
   public enum Side {
      LEFT(RaspiPin.GPIO_24, RaspiPin.GPIO_23),
      RIGHT(RaspiPin.GPIO_26, RaspiPin.GPIO_27);

      private final Pin enablePin;
      private final Pin directionPin;

      Side (Pin enablePin, Pin directionPin) {
         this.enablePin = enablePin;
         this.directionPin = directionPin;
      }

      Pin getEnablePin() {
         return enablePin;
      }

      Pin getDirectionPin() {
         return directionPin;
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

   }

   void initialize() {
      assert gpio != null && !gpio.isShutdown();
      assert leftEnablePin == null;
      assert leftDirectionPin == null;
      assert rightEnablePin == null;
      assert rightDirectionPin == null;

      leftEnablePin = gpio.provisionPwmOutputPin(Side.LEFT.getEnablePin(), "Left E1", 0);
      leftEnablePin.setShutdownOptions(true, PinState.LOW);
      leftEnablePin.setPwmRange(TICKS);

      leftDirectionPin = gpio.provisionDigitalOutputPin(Side.LEFT.getDirectionPin(), "Left M1",
            PinState.LOW);
      leftDirectionPin.setShutdownOptions(true, PinState.LOW);

      rightEnablePin = gpio.provisionPwmOutputPin(Side.RIGHT.getEnablePin(), "Right E1", 0);
      rightEnablePin.setShutdownOptions(true, PinState.LOW);
      rightEnablePin.setPwmRange(TICKS);
      rightDirectionPin = gpio.provisionDigitalOutputPin(Side.RIGHT.getDirectionPin(), "Right M1",
            PinState.LOW);
      rightDirectionPin.setShutdownOptions(true, PinState.LOW);
   }

   public void setSpeed(Side side, float speed) {
      GpioPinPwmOutput enablePin = side == Side.LEFT ? leftEnablePin : rightEnablePin;
      GpioPinDigitalOutput directionPin = side == Side.LEFT ? leftDirectionPin: rightDirectionPin;

      directionPin.setState(speed >= 0 ? PinState.LOW : PinState.HIGH);
      enablePin.setPwm(translateSpeed(speed));
   }

   private int translateSpeed(float speed) {
      assert speed >= -1.0f && speed <= 1.0f;

      float absoluteSpeed = Math.abs(speed);
      float maxValue = (float)TICKS * 0.95f;
      return Math.round(absoluteSpeed * maxValue);
   }
}
