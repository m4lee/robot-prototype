package org.mib.robot.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;
import org.mib.joystick.Event;
import org.mib.joystick.Joystick;
import org.mib.robot.configuration.TestConfigurationModule;
import org.mib.robot.event.EventModule;

import javax.inject.Singleton;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class JoystickServiceTest {

   private TestJoystickComponent joystickComponent;

   @Before
   public void before() {
      joystickComponent = DaggerJoystickServiceTest_TestJoystickComponent.create();
   }

   @Test
   public void translateEvent() {

      Event buttonEvent = new Event(System.currentTimeMillis(), 1, Event.Type.BUTTON, 1, false);

      JoystickEvent translatedButtonEvent  = new JoystickService().translateEvent
            (buttonEvent);

      assertEquals("Event is not a button press", JoystickEvent.Type.Button, translatedButtonEvent.getType());
      assertEquals("The button index is wrong", 1, translatedButtonEvent.getIndex());
      assertTrue("The timestamp is wrong", translatedButtonEvent.getTimestamp() <= System.nanoTime());
      assertTrue("The button is not pressed", translatedButtonEvent.isPressed());

      Event axisEvent = new Event(System.currentTimeMillis(),
            Math.round((float) Event.MAX_VALUE / 4f), Event.Type.AXIS, 2, false);

      double error = 0.5 / Event.MAX_VALUE; // 0.5 from the rounding

      JoystickEvent translatedAxisEvent = new JoystickService().translateEvent(axisEvent);
      assertEquals("Event is not an axis event", JoystickEvent.Type.Axis, translatedAxisEvent
            .getType());
      assertEquals("The axis index is wrong", 2, translatedAxisEvent.getIndex());
      assertTrue("The timestamp is wrong", translatedAxisEvent.getTimestamp() <
            System.nanoTime());
      assertEquals("The value is wrong", 0.25f, translatedAxisEvent.getValue(), error);
   }

   @Test
   public void testStartUp() throws Exception {
      JoystickService service = joystickComponent.joystickService();
      service.startAsync();
      service.awaitRunning(2000, TimeUnit.MILLISECONDS);
      verify(joystickComponent.joystick()).open();
   }

   @Test
   public void testRaiseEvent() throws Exception {

      class TestHandler implements Consumer<JoystickEvent> {
         private JoystickEvent captured;
         @Override
         @Subscribe
         public void accept(JoystickEvent joystickEvent) {
            captured = joystickEvent;
         }
      }

      doCallRealMethod().when(joystickComponent.joystick()).addHandler(any());
      doCallRealMethod().when(joystickComponent.joystick()).raiseEvent(any());
      JoystickService service = joystickComponent.joystickService();
      try {
         service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);

         TestHandler handler = new TestHandler();
         joystickComponent.eventBus().register(handler);

         org.mib.joystick.Event buttonEvent = new Event(System.currentTimeMillis(), 0, Event.Type.BUTTON, 1, false);
         joystickComponent.joystick().raiseEvent(buttonEvent);
         assertNotNull("JoystickEvent not raised.", handler.captured);
      } finally {
         if(service != null && service.isRunning()) {
            service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);
         }
      }
   }

   @Test
   public void testShutdown() {
      joystickComponent.joystickService().shutDown();
      verify(joystickComponent.joystick()).close();
   }

   @Component(modules={MockJoystickModule.class, TestConfigurationModule.class, EventModule.class})
   @Singleton
   interface TestJoystickComponent {
      Joystick joystick();
      JoystickService joystickService();
      EventBus eventBus();
   }
}