package org.mib.robot.input;

import dagger.Module;
import dagger.Provides;
import org.mib.joystick.Joystick;

@Module
public class JoystickModule {

   @Provides
   static Joystick joystick() {
      return new Joystick("/dev/input/js0", new int[] {0, 2});
   }
}
