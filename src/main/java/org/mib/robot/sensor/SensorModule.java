package org.mib.robot.sensor;

import dagger.Module;
import dagger.Provides;
import org.mib.robot.configuration.ConfigurationDirectory;

@Module
class SensorModule {
   @Provides
   RangeFinderConfiguration rangeFinderConfiguration(ConfigurationDirectory directory) {
      return directory.get("rangeFinder", RangeFinderConfiguration.class);
   }
}
