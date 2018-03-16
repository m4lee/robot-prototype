package org.mib.robot.motor;

import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;

import javax.inject.Singleton;

@Module
public class MockMotorModule {
   @Provides
   @Singleton
   static Motor motor()  {
      return Mockito.mock(Motor.class);
   }
}
