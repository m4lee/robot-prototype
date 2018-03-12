package org.mib.robot.input;

import org.junit.Assert;
import org.junit.Test;
import org.mib.robot.DaggerPBot;
import org.mib.robot.PBot;

public class TestDependencyInjection {
   @Test
   public void testConstruction() {
      PBot pBot = DaggerPBot.create();
      Assert.assertNotNull("Component not created", pBot);
      Assert.assertNotNull("JoystickService not created", pBot.joystick());
      Assert.assertNotNull("Joystick not created", pBot.joystick().getJoystick());
      Assert.assertNotNull("Event bus not created", pBot.joystick().getEventBus());
   }
}
