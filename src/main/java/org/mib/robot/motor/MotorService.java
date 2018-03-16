package org.mib.robot.motor;

import com.google.common.eventbus.Subscribe;
import org.mib.robot.event.Event;
import org.mib.robot.event.EventQueueService;

import javax.inject.Inject;
import java.util.logging.Logger;

public class MotorService extends EventQueueService {
   private static final Logger log = Logger.getLogger(MotorService.class.getName());

   @SuppressWarnings("WeakerAccess")
   @Inject Motor motor;

   private static final int LEFT_MOTOR_INDEX = 0;

   @SuppressWarnings("WeakerAccess")
   @Inject MotorService() {
      // allow injection
   }

   @Subscribe
   public void onChangeMotorSpeedEvent(ChangeMotorSpeedEvent event) {
      assert event != null;
      queue.add(event);
   }

   @Override
   protected void startUp() {
      motor.initialize();
      getEventBus().register(this);
   }

   @Override
   protected void handleEvent(Event event) {
      if(event instanceof ChangeMotorSpeedEvent) {
         ChangeMotorSpeedEvent changeEvent = (ChangeMotorSpeedEvent)event;
         Motor.Side side = changeEvent.getMotor() == LEFT_MOTOR_INDEX ? Motor.Side.LEFT : Motor.Side.RIGHT;

         log.fine("Setting " + side + " motor to " + changeEvent.getSpeed());
         motor.setSpeed(side, changeEvent.getSpeed());
      }
   }
}
