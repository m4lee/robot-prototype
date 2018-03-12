package org.mib.robot;

import dagger.Component;
import org.mib.robot.event.EventModule;
import org.mib.robot.input.JoystickModule;
import org.mib.robot.input.JoystickService;

import javax.inject.Singleton;

@Component(modules={JoystickModule.class,
      EventModule.class})
@Singleton
public interface PBot {
   JoystickService joystick();
}
