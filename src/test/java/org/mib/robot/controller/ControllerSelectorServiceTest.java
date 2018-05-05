package org.mib.robot.controller;

import com.google.common.eventbus.EventBus;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mib.robot.event.EventModule;
import org.mib.robot.input.JoystickEvent;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

public class ControllerSelectorServiceTest {

   private TestControllerSelectorComponent component;
   private ControllerSelectorService service;

   private static final int BUTTON_A = 0;
   private static final int BUTTON_B = 1;

   private static final String CONTROLLER_A_ID = "A";
   private static final String CONTROLLER_B_ID = "B";

   @Before
   public void before() {
      component = DaggerControllerSelectorServiceTest_TestControllerSelectorComponent.create();
      service = component.service();
   }

   @After
   public void cleanup() throws TimeoutException {
      service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);
   }

   @Test
   public void testSwitch() throws Exception {
      TestController a = new TestController(CONTROLLER_A_ID);
      TestController b = new TestController(CONTROLLER_B_ID);

      a.startUp(component.eventBus());
      b.startUp(component.eventBus());

      service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);

      assertTrue("default service started", a.waitStarted(true, 2000, TimeUnit.MILLISECONDS));

      component.eventBus().post(new JoystickEvent(BUTTON_B, true));

      assertTrue("service stopped", a.waitStarted(false, 20000, TimeUnit.MILLISECONDS));
      assertTrue("service started", b.waitStarted(true, 20000, TimeUnit.MILLISECONDS));
   }

   @Module
   @SuppressWarnings("WeakerAccess")
   static class TestControllerModule {
      @Provides
      static SelectorConfiguration selectorConfiguration() {
         SelectorConfiguration configuration = new SelectorConfiguration();
         Map<Integer, String> buttonControllerMap = new HashMap<>();
         buttonControllerMap.put(BUTTON_A, CONTROLLER_A_ID);
         buttonControllerMap.put(BUTTON_B, CONTROLLER_B_ID);
         configuration.setButtonControllerMap(buttonControllerMap);
         configuration.setDefaultController(CONTROLLER_A_ID);
         return configuration;
      }
   }

   @Singleton
   @Component(modules={TestControllerModule.class, EventModule.class})
   interface TestControllerSelectorComponent {
      ControllerSelectorService service();
      EventBus eventBus();
   }
}
