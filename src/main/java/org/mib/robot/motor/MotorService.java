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

   @SuppressWarnings("WeakerAccess")
   @Inject MotorConfiguration configuration;

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
      assert configuration != null;

      if(configuration.getInstance() != null) {
         configuration.getInstance().forEach(i -> motor.registerInstance(i.getIndex(), i.getName(),
               i.getEnablePin(), i.getDirectionPin(), i.isInvert(), configuration.getPwmPeriod(),
               i.getMinPwm()));
      }
      getEventBus().register(this);
   }

   @Override
   protected void handleEvent(Event event) {
      if(event instanceof ChangeMotorSpeedEvent) {
         ChangeMotorSpeedEvent changeEvent = (ChangeMotorSpeedEvent)event;

         log.fine("Setting motor @ " + changeEvent.getMotor() + " to " + changeEvent.getSpeed());
         motor.setSpeed(changeEvent.getMotor(), changeEvent.getSpeed());
         getEventBus().post(new MotorSpeedUpdatedEvent(changeEvent.getMotor()));
      }
   }
}
