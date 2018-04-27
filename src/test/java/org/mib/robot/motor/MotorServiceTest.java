package org.mib.robot.motor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;
import org.mib.robot.configuration.TestConfigurationModule;
import org.mib.robot.event.EventModule;

import javax.inject.Singleton;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MotorServiceTest {

   private TestMotorComponent motorComponent;

   @Before
   public void before() {
      motorComponent = DaggerMotorServiceTest_TestMotorComponent.create();
   }

   @Test
   public void testStartUp() throws Exception {
      MotorService service = motorComponent.service();
      service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);
      verify(motorComponent.motor(), times(2)).registerInstance(anyInt(), anyString(), anyInt(),
            anyInt(),
            anyBoolean(), anyInt(), anyFloat());
   }

   @Test
   public void testHandleEvent() throws Exception {
      class MotorEventHandler {
         private MotorSpeedUpdatedEvent captured;
         private CountDownLatch latch;

         MotorEventHandler() {
            reset();
         }

         @Subscribe
         void onSpeedUpdated(MotorSpeedUpdatedEvent event) {
            captured = event;
            latch.countDown();
         }

         void reset() {
            captured = null;
            latch = new CountDownLatch(1);
         }
      }

      MotorEventHandler motorEventHandler = new MotorEventHandler();
      motorComponent.eventBus().register(motorEventHandler);

      MotorService service = motorComponent.service();
      try {
         service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);

         ChangeMotorSpeedEvent leftForwardEvent = new ChangeMotorSpeedEvent(0, 0.5f);
         motorComponent.eventBus().post(leftForwardEvent);

         motorEventHandler.latch.await(2000, TimeUnit.MILLISECONDS);

         verify(motorComponent.motor()).setSpeed(0, 0.5f);
         assertNotNull("no motor speed updated event raised", motorEventHandler.captured);
         assertEquals("udpate event raised for wrong motor", 0, motorEventHandler.captured
               .getMotor());

         motorEventHandler.reset();

         ChangeMotorSpeedEvent rightBackEvent = new ChangeMotorSpeedEvent(1, -0.5f);
         motorComponent.eventBus().post(rightBackEvent);

         motorEventHandler.latch.await(2000, TimeUnit.MILLISECONDS);

         verify(motorComponent.motor()).setSpeed(1, -0.5f);
         assertNotNull("no motor speed updated event raised", motorEventHandler.captured);
         assertEquals("udpate event raised for wrong motor", 1, motorEventHandler.captured
               .getMotor());
      } finally {
         service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);
      }
   }


   @Component(modules = {MockMotorModule.class, TestConfigurationModule.class, EventModule.class})
   @Singleton
   interface TestMotorComponent {
      Motor motor();
      MotorService service();
      EventBus eventBus();
   }
}
