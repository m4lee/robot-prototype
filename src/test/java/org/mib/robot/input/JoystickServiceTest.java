package org.mib.robot.input;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;
import org.mib.joystick.Event;
import org.mib.joystick.Joystick;
import org.mib.robot.event.EventModule;
import org.mockito.ArgumentCaptor;

import javax.inject.Singleton;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JoystickServiceTest {

   private TestJoystickComponent joystickComponent;

   private Event createMockEvent(Event.Type type, int axisOrButton, long time, int value) {
      Event event = mock(org.mib.joystick.Event.class);
      when(event.getEventType()).thenReturn(type);
      when(event.getAxisOrButton()).thenReturn(axisOrButton);
      when(event.getTime()).thenReturn(time);
      when(event.getValue()).thenReturn(value);
      return event;
   }

   @Test
   public void translateEvent() {
      Event buttonEvent = createMockEvent(Event.Type.BUTTON, 1, 1L, 1);

      JoystickEvent translatedButtonEvent  = new JoystickService().translateEvent(buttonEvent);

      assertEquals("Event is not a button press", JoystickEvent.Type.Button, translatedButtonEvent.getType());
      assertEquals("The button index is wrong", 1, translatedButtonEvent.getIndex());
      assertTrue("The timestamp is wrong", translatedButtonEvent.getTimestamp() <= System.nanoTime());
      assertTrue("The button is not pressed", translatedButtonEvent.isPressed());

      Event axisEvent = createMockEvent(Event.Type.AXIS, 2, 2L,
            Math.round((float)org.mib.joystick.Event.MAX_VALUE / 4f));

      double error = 0.5 / Event.MAX_VALUE; // 0.5 from the rounding

      JoystickEvent translatedAxisEvent = new JoystickService().translateEvent(axisEvent);
      assertEquals("Event is not an axis event", JoystickEvent.Type.Axis, translatedAxisEvent
            .getType());
      assertEquals("The axis index is wrong", 2, translatedAxisEvent.getIndex());
      assertTrue("The timestamp is wrong", translatedAxisEvent.getTimestamp() <
            System.nanoTime());
      assertEquals("The value is wrong", 0.25f, translatedAxisEvent.getValue(), error);
   }

   @Before
   public void before() {
      joystickComponent = DaggerJoystickServiceTest_TestJoystickComponent.create();
   }

   @Test
   public void testStartUp() throws Exception {
      joystickComponent.joystickService().startUp();
      verify(joystickComponent.joystick()).open();
   }

   @Test
   public void testRaiseEvent() {

      class TestHandler implements Consumer<JoystickEvent> {
         private JoystickEvent captured;
         @Override
         @Subscribe
         public void accept(JoystickEvent joystickEvent) {
            captured = joystickEvent;
         }
      }

      joystickComponent.joystickService().startUp();

      @SuppressWarnings("unchecked")
      ArgumentCaptor<Consumer<Event>> handlerCapture = ArgumentCaptor.forClass(Consumer.class);

      verify(joystickComponent.joystick()).addHandler(handlerCapture.capture());

      TestHandler handler = new TestHandler();
      joystickComponent.eventBus().register(handler);

      org.mib.joystick.Event buttonEvent = createMockEvent(Event.Type.BUTTON, 1, 1L, 0);
      handlerCapture.getValue().accept(buttonEvent);

      assertNotNull("JoystickEvent not raised.", handler.captured);
   }

   @Test
   public void testShutdown() {
      joystickComponent.joystickService().shutDown();
      verify(joystickComponent.joystick()).close();
   }

   @Component(modules={MockJoystickModule.class, EventModule.class})
   @Singleton
   interface TestJoystickComponent {
      JoystickService joystickService();
      EventBus eventBus();
      Joystick joystick();
   }
}