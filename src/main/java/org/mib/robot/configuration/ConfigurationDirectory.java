package org.mib.robot.configuration;

import com.moandjiezana.toml.Toml;

import java.io.File;

public class ConfigurationDirectory {
   private Toml storage;

   public void load(String filename) {
      storage = new Toml().read(new File(filename));
   }

   public <T> T get(String key, Class<T> type) {
      return storage.getTable(key).to(type);
   }
}
