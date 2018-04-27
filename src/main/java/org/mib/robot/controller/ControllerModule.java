package org.mib.robot.controller;

import dagger.Module;
import dagger.Provides;
import org.mib.robot.configuration.ConfigurationDirectory;

@Module
public class ControllerModule {
   @Provides
   ControllerConfiguration configuration(ConfigurationDirectory configurationDirectory) {
      return configurationDirectory.get(ControllerService.ID, ControllerConfiguration.class);
   }
}
