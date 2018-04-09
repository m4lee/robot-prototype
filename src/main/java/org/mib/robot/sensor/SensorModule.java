package org.mib.robot.sensor;

import dagger.Module;
import dagger.Provides;
import org.mib.robot.configuration.ConfigurationDirectory;

@Module
public class SensorModule {
   @Provides
   RangeFinderConfiguration rangeFinderConfiguration(ConfigurationDirectory directory) {
      return directory.get("rangeFinder", RangeFinderConfiguration.class);
   }
}
