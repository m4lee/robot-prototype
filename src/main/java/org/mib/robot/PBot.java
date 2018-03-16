package org.mib.robot;

import dagger.Component;
import org.mib.robot.event.EventModule;
import org.mib.robot.input.JoystickModule;
import org.mib.robot.pi.GpioModule;

import javax.inject.Singleton;

@Component(modules={JoystickModule.class,
      GpioModule.class,
      EventModule.class})
@Singleton
public interface PBot {
   Bootstrap bootstrap();
}
