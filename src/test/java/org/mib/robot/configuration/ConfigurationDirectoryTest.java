package org.mib.robot.configuration;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ConfigurationDirectoryTest {

   @SuppressWarnings("unused")
   private static class WithArray {
      String simple;
      int[] array;
   }

   @SuppressWarnings("unused")
   private static class Compound {
      String name;
      List<Instance> instance;
   }

   @SuppressWarnings("unused")
   private static class Instance {
      int id;
   }

   private ConfigurationDirectory directory;

   @Before
   public void createConfigurationDirectory() {
      directory = new ConfigurationDirectory();
   }

   @Test
   public void testGet() {
      directory.load("src/test/resources/org/mib/robot/configuration/unittest-config.toml");
      WithArray withArray = directory.get("with-array", WithArray.class);
      assertNotNull(withArray);
      assertEquals("simple member is wrong value", "test", withArray.simple);
      assertArrayEquals("array is different", new int[] {1, 2, 3}, withArray.array);

      Compound compound = directory.get("compound", Compound.class);
      assertNotNull(compound);
      assertEquals("name is incorrect", "identifier", compound.name);
      assertNotNull("list is missing", compound.instance);
      assertTrue("instances is too small", compound.instance.size() > 1);
      assertEquals("first element is wrong", 1, compound.instance.get(0).id);
      assertEquals("second element is wrong", 2, compound.instance.get(1).id);
   }
}