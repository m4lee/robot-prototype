package org.mib.robot.input;

import dagger.Module;
import dagger.Provides;
import org.mib.joystick.Joystick;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@Module
public class MockJoystickModule {
   @Provides @Singleton
   static Joystick joystick() {
      return mock(Joystick.class, withSettings().useConstructor("notafile", null));
   }
}
