package org.mib.robot.configuration;

import dagger.Module;
import dagger.Provides;

import javax.annotation.Nullable;

@Module
public class ConfigurationModule {
   private static final String DEFAULT = "etc/config.toml";

   @Provides
   static ConfigurationDirectory directory(@ConfigFile @Nullable String configurationFile) {
      ConfigurationDirectory directory = new ConfigurationDirectory();
      directory.load(configurationFile != null ? configurationFile : DEFAULT);
      return directory;
   }
}
