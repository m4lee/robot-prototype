package org.mib.robot.motor;

import dagger.Module;
import dagger.Provides;
import org.mib.robot.configuration.ConfigurationDirectory;

@Module
public class MotorModule {
   @Provides
   MotorConfiguration motorConfiguration(ConfigurationDirectory configuration) {
      return configuration.get("motor", MotorConfiguration.class);
   }
}
