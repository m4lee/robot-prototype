package org.mib.robot.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;
import org.mib.robot.event.EventModule;
import org.mib.robot.input.JoystickEvent;
import org.mib.robot.motor.ChangeMotorSpeedEvent;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ControllerServiceTest {
   private TestControllerComponent controllerComponent;

   @Test
   public void testHandleEvent() throws Exception {
      ControllerService service = controllerComponent.controller();
      service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);

      // post a joystick event and make sure that a change motor speed event is sent out
      class TestHandler {
         private ChangeMotorSpeedEvent captured;
         @Subscribe
         public void onChangeMotorSpeedEvent(ChangeMotorSpeedEvent event) {
            this.captured = event;
         }
      }

      TestHandler handler = new TestHandler();
      controllerComponent.eventBus().register(handler);

      JoystickEvent joystickEvent = new JoystickEvent(1, 0.5f);
      controllerComponent.eventBus().post(joystickEvent);

      service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);

      assertNotNull("No change motor speed event raised.", handler.captured);
   }

   @Test
   public void testMapToMotor() {
      ControllerService service = controllerComponent.controller();
      assertEquals("Axis didn't map to left motor", 0, service.mapToMotor(1));
      assertEquals("Axis didn't map to right motor", 1, service.mapToMotor(5));
   }

   @Before
   public void before() {
      controllerComponent = DaggerControllerServiceTest_TestControllerComponent.create();
   }

   @Component(modules={EventModule.class})
   @Singleton
   interface TestControllerComponent {
      ControllerService controller();
      EventBus eventBus();
   }
}
