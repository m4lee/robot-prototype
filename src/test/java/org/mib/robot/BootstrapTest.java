package org.mib.robot;

import com.google.common.util.concurrent.Service;
import com.pi4j.io.gpio.GpioController;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.junit.Before;
import org.junit.Test;
import org.mib.joystick.Event;
import org.mib.joystick.Joystick;
import org.mib.robot.configuration.ConfigurationDirectory;
import org.mib.robot.configuration.TestConfigurationModule;
import org.mib.robot.controller.ControllerModule;
import org.mib.robot.event.EventModule;
import org.mib.robot.input.MockJoystickModule;
import org.mib.robot.motor.MockMotorModule;
import org.mib.robot.motor.Motor;
import org.mib.robot.pi.GpioConfiguration;
import org.mib.robot.sensor.MockUs100Module;
import org.mib.robot.sensor.SensorModule;
import org.mockito.Mockito;

import javax.inject.Singleton;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class BootstrapTest {

   private TestBootstrap bootstrapComponent;

   @Before
   public void before() {
      bootstrapComponent = DaggerBootstrapTest_TestBootstrap.create();
   }

   @Test
   public void integrationTest() throws TimeoutException {
      final int JOYSTICK_VALUE = 1024;
      final float EXPECTED_MOTOR_VALUE = (float)JOYSTICK_VALUE / Event.MAX_VALUE;

      Bootstrap bootstrap = bootstrapComponent.bootstrap();
      doCallRealMethod().when(bootstrapComponent.joystick()).addHandler(any());
      doCallRealMethod().when(bootstrapComponent.joystick()).setAxes(any());
      doCallRealMethod().when(bootstrapComponent.joystick()).raiseEvent(any());
      try {
         bootstrap.start();

         // try raising some joystick events and confirm that the motor receives the instructions.
         Event leftForwardEvent = new Event(0, -JOYSTICK_VALUE, Event.Type.AXIS, 1, false);
         bootstrapComponent.joystick().raiseEvent(leftForwardEvent);

         Event leftBackEvent = new Event(0, JOYSTICK_VALUE, Event.Type.AXIS, 1, false);
         bootstrapComponent.joystick().raiseEvent(leftBackEvent);

         Event rightForwardEvent = new Event(0, -JOYSTICK_VALUE, Event.Type.AXIS, 5, false);
         bootstrapComponent.joystick().raiseEvent(rightForwardEvent);

         Event rightBackEvent = new Event(0, JOYSTICK_VALUE, Event.Type.AXIS, 5, false);
         bootstrapComponent.joystick().raiseEvent(rightBackEvent);

         // raise a spurious joystick event and confirm that it's not called.
         Event invalidEvent = new Event(0, JOYSTICK_VALUE, Event.Type.AXIS, 0, false);
         bootstrapComponent.joystick().raiseEvent(invalidEvent);

         Motor motor = bootstrapComponent.motor();

         verify(motor, timeout(500)).setSpeed(0, EXPECTED_MOTOR_VALUE);
         verify(motor, timeout(500)).setSpeed(0, -EXPECTED_MOTOR_VALUE);
         verify(motor, timeout(500)).setSpeed(1, EXPECTED_MOTOR_VALUE);
         verify(motor, timeout(500)).setSpeed(1, -EXPECTED_MOTOR_VALUE);

      } finally {
         bootstrap.stop();
      }
   }

   @Test
   public void testAddServiceLoggerListener() {
      Service service = Mockito.mock(Service.class);
      bootstrapComponent.bootstrap().addServiceLoggerListener(service);
      verify(service).addListener(any(), any());
   }

   // we want to test the GpioService but not interact with the GPIO at all
   @SuppressWarnings("WeakerAccess")
   @Module
   static class MockGpioModule {
      @Provides
      @SuppressWarnings("unused")
      static GpioController gpio() {
         return mock(GpioController.class);
      }

      @Provides static GpioConfiguration gpioConfiguration(ConfigurationDirectory directory) {
         return directory.get("pi", GpioConfiguration.class);
      }
   }

   @Component(modules={MockJoystickModule.class,
         MockGpioModule.class,
         MockMotorModule.class,
         MockUs100Module.class,
         ControllerModule.class,
         SensorModule.class,
         EventModule.class,
         TestConfigurationModule.class})
   @Singleton
   interface TestBootstrap {
      Bootstrap bootstrap();
      Motor motor();
      Joystick joystick();
   }
}
