package org.mib.robot.motor;

import dagger.Module;
import dagger.Provides;
import org.mib.robot.configuration.ConfigurationDirectory;
import org.mockito.Mockito;

import javax.inject.Singleton;

@Module
public class MockMotorModule {
   @Provides
   @Singleton
   static Motor motor()  {
      return Mockito.mock(Motor.class);
   }

   @Provides
   static MotorConfiguration motorConfiguration(ConfigurationDirectory directory) {
      return directory.get("motor", MotorConfiguration.class);
   }
}
