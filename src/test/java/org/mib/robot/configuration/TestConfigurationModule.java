package org.mib.robot.configuration;

import dagger.Module;
import dagger.Provides;

@Module
public class TestConfigurationModule {
   @Provides
   static ConfigurationDirectory configurationDirectory() {
      ConfigurationDirectory directory = new ConfigurationDirectory();
      directory.load("src/test/conf/test-config.toml");
      return directory;
   }
}
