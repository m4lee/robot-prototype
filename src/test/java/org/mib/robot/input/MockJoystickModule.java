package org.mib.robot.input;

import dagger.Module;
import dagger.Provides;
import org.mib.joystick.Joystick;
import org.mockito.Mockito;

import javax.inject.Singleton;

@Module
class MockJoystickModule {
   @Provides @Singleton
   static Joystick joystick() {
      return Mockito.mock(Joystick.class);
   }
}
