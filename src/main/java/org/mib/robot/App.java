package org.mib.robot;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.trigger.GpioTriggerBase;
import com.pi4j.wiringpi.Gpio;
import org.mib.joystick.Joystick;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Try turning on a pin
 *
 */
public class App 
{

   public static void main( String[] args ) throws Exception {
      //turnOnPwm(args);
      //turnOnPin();
      //receiveDatagrams();
      //handleInterrupts();
      //keyboardPwmControl();
      readJoystick();
   }

   private static void readJoystick() throws Exception {
      try(Joystick joystick = new Joystick("/dev/input/js0", new int[] { 1, 5 },
            System.out::println)) {
         joystick.open();
         while(true) {
            Thread.sleep(10000);
         }
      }
   }

   private static void keyboardPwmControl() throws Exception {
      final float MAX_VALUE =  512f * 0.95f; // limit to 95% duty cycle to be nice to motors

      GpioController gpio = GpioFactory.getInstance();

      try {
         // initialize PWM hardware
         Gpio.pwmSetClock(16); // divide 19.2MHz down to 1.2MHz
         Gpio.pwmSetMode(Gpio.PWM_MODE_MS);

         // setup PWM pin to drive E1 pin of L298
         GpioPinPwmOutput pwmPin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_24, "E1-L298", 0);
         pwmPin.setShutdownOptions(true, PinState.LOW);
         pwmPin.setPwmRange(512); // set a range of 512 which gives us 512 different duty cycles
         // and  a PWM frequency of 2.34khz

         // setup IO pin to drive M1 pin of L298
         GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "M1-L298",
               PinState.LOW);
         ledPin.setShutdownOptions(true, PinState.LOW);

         while (true) {
            char c = (char) System.console().reader().read();
            if (Character.isDigit(c)) {
               int level = Character.digit(c, 10);
               float activeCount = MAX_VALUE * (float) level / 9f;

               System.out.println("Setting to " + Math.round(activeCount));
               pwmPin.setPwm(Math.round(activeCount));
            }
         }
      } finally {
         gpio.shutdown();
      }

   }

   private static void handleInterrupts() throws Exception {
      GpioController gpio = GpioFactory.getInstance();
      try {
         GpioPinDigitalInput input = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02,
               PinPullResistance.OFF);
         System.out.println("hasDebounce(LOW) = " + input.hasDebounce(PinState.LOW));
         System.out.println("hasDebounce(HIGH) = " + input.hasDebounce(PinState.HIGH));
         input.setShutdownOptions(true, PinState.LOW);

         input.addTrigger(new GpioTriggerBase() {
            @Override
            public void invoke(GpioPin pin, PinState state) {
               System.out.println("Pin " + pin + " set to " + state + ".");
            }
         });

         while (true) {
            Thread.sleep(1000);
         }
      } finally {
         gpio.shutdown();
      }
   }

   private static void receiveDatagrams() throws Exception {
      final int BUFFER_LENGTH = 2048;
      DatagramPacket packet = new DatagramPacket(new byte[BUFFER_LENGTH], 0, BUFFER_LENGTH);

      GpioController gpio = GpioFactory.getInstance();

      try (DatagramSocket socket = new DatagramSocket(5000)){
         GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "My LED",
               PinState.LOW);
         ledPin.setShutdownOptions(true, PinState.LOW);

         while(true) {
            socket.receive(packet);
            System.out.print("Message received:");
            for(int i = 0; i < packet.getLength(); i++) {
               System.out.print(Integer.toHexString(packet.getData()[i]));
               ledPin.pulse(500, false);
            }
            System.out.print('\n');
         }
      } finally {
         gpio.shutdown();
      }
   }

   private static void turnOnPwm(String[] args) throws InterruptedException {
      GpioController gpio = GpioFactory.getInstance();
      try {
         GpioPinPwmOutput pwmPin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_24, "PWM0", 0);

         pwmPin.setShutdownOptions(true, PinState.LOW);
         Gpio.pwmSetClock(16); // divide 19.2MHz down to 1.2MHz
         Gpio.pwmSetMode(Gpio.PWM_MODE_MS);

         pwmPin.setPwmRange(512); // set a range of 512 which gives us 512 different duty cycles
         // and  a PWM frequency of 2.34khz
         pwmPin.setPwm(128);
         Thread.sleep(5000);
      } finally {
         gpio.shutdown();
      }

   }

   private static void turnOnPin() throws InterruptedException {
      GpioController gpio = GpioFactory.getInstance();
      try {
         GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "My LED",
               PinState.LOW);
         ledPin.setShutdownOptions(true, PinState.LOW);

         ledPin.high();

         Thread.sleep(1000);
      } finally {
         gpio.shutdown();
      }
   }
}
