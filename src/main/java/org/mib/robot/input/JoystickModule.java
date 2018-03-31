package org.mib.robot.input;

import dagger.Module;
import dagger.Provides;
import org.mib.joystick.Joystick;
import org.mib.robot.configuration.ConfigurationDirectory;

@Module
public class JoystickModule {

   @Provides
   static Joystick joystick() {
      return new Joystick("/dev/input/js0");
   }

   @Provides
   static JoystickConfiguration configuration(ConfigurationDirectory directory) {
      return directory.get("joystick", JoystickConfiguration.class);
   }
}
