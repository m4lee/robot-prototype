package org.mib.robot.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dagger.Component;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mib.robot.configuration.TestConfigurationModule;
import org.mib.robot.event.EventModule;
import org.mib.robot.input.JoystickEvent;
import org.mib.robot.motor.ChangeMotorSpeedEvent;
import org.mib.robot.motor.MotorSpeedUpdatedEvent;

import javax.inject.Singleton;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ControllerServiceTest {
   private TestControllerComponent controllerComponent;
   private ControllerService service;

   private static class SpeedHandler {
      private ChangeMotorSpeedEvent captured;
      private CountDownLatch latch = new CountDownLatch(1);
      @Subscribe
      public void onChangeMotorSpeedEvent(ChangeMotorSpeedEvent event) {
         this.captured = event;
         latch.countDown();
      }

      private void reset() {
         captured = null;
         latch = new CountDownLatch(1);
      }
   }

   @Before
   public void before()  throws TimeoutException {
      controllerComponent = DaggerControllerServiceTest_TestControllerComponent.create();
      service = controllerComponent.controller();
      service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);
   }

   @After
   public void cleanup() throws TimeoutException {
      service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);
   }

   @Test
   public void testHandleEvent() throws Exception {

      // post a joystick event and make sure that a change motor speed event is sent out
      SpeedHandler handler = new SpeedHandler();
      controllerComponent.eventBus().register(handler);

      ControllerControlEvent controllerControlEvent = new ControllerControlEvent(
            ControllerControlEvent.Instruction.START, ControllerService.ID);
      controllerComponent.eventBus().post(controllerControlEvent);

      JoystickEvent joystickEvent = new JoystickEvent(1, 0.5f);
      controllerComponent.eventBus().post(joystickEvent);

      handler.latch.await(2000, TimeUnit.MILLISECONDS);

      assertNotNull("No change motor speed event raised.", handler.captured);
   }

   @Test
   public void testControl() throws Exception {
      // when the service starts up it should not respond to inputs
      SpeedHandler speedHandler = new SpeedHandler();
      controllerComponent.eventBus().register(speedHandler);

      JoystickEvent joystickEvent = new JoystickEvent(1, 0.5f);
      controllerComponent.eventBus().post(joystickEvent);

      speedHandler.latch.await(100, TimeUnit.MILLISECONDS);

      assertNull("A speed change event was raised", speedHandler.captured);

      // start then stop the service
      ControlHandler controlHandler = new ControlHandler();
      controllerComponent.eventBus().register(controlHandler);

      ControllerControlEvent controllerControlEvent = new ControllerControlEvent(
            ControllerControlEvent.Instruction.START, ControllerService.ID);
      controllerComponent.eventBus().post(controllerControlEvent);

      controlHandler.latch.await(2000, TimeUnit.MILLISECONDS);
      assertNotNull("control event not raised", controlHandler.captured);
      assertEquals("control message from wrong controller", ControllerService.ID,
            controlHandler.captured.getId());
      assertEquals("wrong control message instruction", ControllerControlEvent.Instruction.STARTED,
            controlHandler.captured.getInstruction());

      controlHandler.reset();
      controllerControlEvent = new ControllerControlEvent(
            ControllerControlEvent.Instruction.STOP, ControllerService.ID);
      controllerComponent.eventBus().post(controllerControlEvent);

      // post the motor speed updated events to simulate the actual motor service
      controllerComponent.eventBus().post(new MotorSpeedUpdatedEvent(0));
      controllerComponent.eventBus().post(new MotorSpeedUpdatedEvent(1));

      controlHandler.latch.await(2000, TimeUnit.MILLISECONDS);
      assertNotNull("stop event not raised", controlHandler.captured);
      assertEquals("wrong control message instruction", ControllerControlEvent.Instruction.STOPPED,
            controlHandler.captured.getInstruction());

      // post an input and make sure that no control event is raised in response
      speedHandler.reset();
      controllerComponent.eventBus().post(joystickEvent);

      speedHandler.latch.await(100, TimeUnit.MILLISECONDS);
      assertNull("The joystick event was ignored", speedHandler.captured);

   }

   @Test
   public void testMapToMotor() {
      assertEquals("Axis didn't map to left motor", 0, service.mapToMotor(1));
      assertEquals("Axis didn't map to right motor", 1, service.mapToMotor(5));
   }


   @Component(modules={ControllerModule.class, TestConfigurationModule.class, EventModule.class})
   @Singleton
   interface TestControllerComponent {
      ControllerService controller();
      EventBus eventBus();
   }
}
