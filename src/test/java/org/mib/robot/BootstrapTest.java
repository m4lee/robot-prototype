package org.mib.robot;

import com.pi4j.io.gpio.GpioController;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.junit.Test;
import org.mib.joystick.Event;
import org.mib.joystick.Joystick;
import org.mib.robot.event.EventModule;
import org.mib.robot.input.MockJoystickModule;
import org.mib.robot.motor.MockMotorModule;
import org.mib.robot.motor.Motor;

import javax.inject.Singleton;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class BootstrapTest {

   @Test
   public void integrationTest() throws TimeoutException{
      final int JOYSTICK_VALUE = 1024;
      final float EXPECTED_MOTOR_VALUE = (float)JOYSTICK_VALUE / Event.MAX_VALUE;

      TestBootstrap testBootstrap = DaggerBootstrapTest_TestBootstrap.create();
      Bootstrap bootstrap = testBootstrap.bootstrap();
      doCallRealMethod().when(testBootstrap.joystick()).addHandler(any());
      doCallRealMethod().when(testBootstrap.joystick()).raiseEvent(any());
      try {
         bootstrap.start();

         // try raising some joystick events and confirm that the motor receives the instructions.
         Event leftForwardEvent = new Event(0, JOYSTICK_VALUE, Event.Type.AXIS, 0, false);
         testBootstrap.joystick().raiseEvent(leftForwardEvent);

         Event leftBackEvent = new Event(0, -JOYSTICK_VALUE, Event.Type.AXIS, 0, false);
         testBootstrap.joystick().raiseEvent(leftBackEvent);

         Event rightForwardEvent = new Event(0, JOYSTICK_VALUE, Event.Type.AXIS, 2, false);
         testBootstrap.joystick().raiseEvent(rightForwardEvent);

         Event rightBackEvent = new Event(0, -JOYSTICK_VALUE, Event.Type.AXIS, 2, false);
         testBootstrap.joystick().raiseEvent(rightBackEvent);
         Motor motor = testBootstrap.motor();

         verify(motor, timeout(500)).setSpeed(Motor.Side.LEFT, EXPECTED_MOTOR_VALUE);
         verify(motor, timeout(500)).setSpeed(Motor.Side.LEFT, -EXPECTED_MOTOR_VALUE);
         verify(motor, timeout(500)).setSpeed(Motor.Side.RIGHT, EXPECTED_MOTOR_VALUE);
         verify(motor, timeout(500)).setSpeed(Motor.Side.RIGHT, -EXPECTED_MOTOR_VALUE);

      } finally {
         bootstrap.stop();
      }
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
   }

   @Component(modules={MockJoystickModule.class,
         MockGpioModule.class,
         MockMotorModule.class,
         EventModule.class})
   @Singleton
   interface TestBootstrap {
      Bootstrap bootstrap();
      Motor motor();
      Joystick joystick();
   }
}
