package org.mib.robot.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.wiringpi.Gpio;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class GpioModule {
   @SuppressWarnings("unused")
   public static final int PWM_CLOCK_FREQUENCY = 19_200_000; // The base PWM clock is 19.2 MHz
   @SuppressWarnings({"WeakerAccess", "unused"})
   public static final int CLOCK_DIVISOR = 8;

   @Provides @Singleton static GpioController gpio() {
      GpioController gpio = GpioFactory.getInstance();
      Gpio.pwmSetClock(CLOCK_DIVISOR); // divide 19.2MHz down to 1.2MHz
      Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
      return gpio;
   }
}
