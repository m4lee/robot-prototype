package org.mib.robot.motor;

import com.google.common.eventbus.EventBus;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;
import org.mib.robot.event.EventModule;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

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
      verify(motorComponent.motor()).initialize();
   }

   @Test
   public void testHandleEvent() throws Exception {
      MotorService service = motorComponent.service();
      service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);

      ChangeMotorSpeedEvent leftForwardEvent = new ChangeMotorSpeedEvent(0, 0.5f);
      motorComponent.eventBus().post(leftForwardEvent);

      service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);

      verify(motorComponent.motor()).setSpeed(Motor.Side.LEFT, 0.5f);

      service = motorComponent.service();
      service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);

      ChangeMotorSpeedEvent rightBackEvent = new ChangeMotorSpeedEvent(1, -0.5f);
      motorComponent.eventBus().post(rightBackEvent);

      service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);

      verify(motorComponent.motor()).setSpeed(Motor.Side.RIGHT, -0.5f);
   }


   @Component(modules = {MockMotorModule.class, EventModule.class})
   @Singleton
   interface TestMotorComponent {
      Motor motor();
      MotorService service();
      EventBus eventBus();
   }
}
