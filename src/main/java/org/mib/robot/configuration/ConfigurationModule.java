package org.mib.robot.configuration;

import dagger.Module;
import dagger.Provides;

@Module
public class ConfigurationModule {
   @Provides
   static ConfigurationDirectory directory() {
      ConfigurationDirectory directory = new ConfigurationDirectory();
      directory.load("conf/config.toml");
      return directory;
   }
}
