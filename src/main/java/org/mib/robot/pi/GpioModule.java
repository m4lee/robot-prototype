package org.mib.robot.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.wiringpi.Gpio;
import dagger.Module;
import dagger.Provides;
import org.mib.robot.configuration.ConfigurationDirectory;

import javax.inject.Singleton;

@Module
public class GpioModule {
   @SuppressWarnings("unused")
   public static final int PWM_CLOCK_FREQUENCY = 19_200_000; // The base PWM clock is 19.2 MHz
   private static final String PWM_MARK_SPACE_MODE = "PWM_MODE_MS";

   @Provides @Singleton static GpioController gpio(GpioConfiguration configuration) {
      GpioController gpio = GpioFactory.getInstance();

      Gpio.pwmSetClock(configuration.getClockDivisor());
      int pwmMode = PWM_MARK_SPACE_MODE.equals(configuration.getPwmMode()) ? Gpio.PWM_MODE_MS :
            Gpio.PWM_MODE_BAL;
      Gpio.pwmSetMode(pwmMode);
      return gpio;
   }

   @Provides static GpioConfiguration gpioConfiguration(ConfigurationDirectory directory) {
      return directory.get("pi", GpioConfiguration.class);
   }
}
