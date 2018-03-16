package org.mib.robot.controller;

import com.google.common.eventbus.Subscribe;
import org.mib.robot.event.Event;
import org.mib.robot.event.EventQueueService;
import org.mib.robot.input.JoystickEvent;
import org.mib.robot.motor.ChangeMotorSpeedEvent;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ControllerService extends EventQueueService {

   private final Map<Integer, Integer> axisMotorMap = new HashMap<>();

   @Inject
   ControllerService() {
      axisMotorMap.put(0, 0);
      axisMotorMap.put(2, 1);
   }

   @Subscribe
   public void onJoystickEvent(JoystickEvent event) {
      queue.add(event);
   }

   @Override
   public void startUp() {
      getEventBus().register(this);
   }

   @Override
   protected void handleEvent(Event event) {
      if(event instanceof JoystickEvent) {
         JoystickEvent joystickEvent = (JoystickEvent)event;
         if(joystickEvent.getType() == JoystickEvent.Type.Axis) {
            // translate the axis to a motor
            getEventBus().post(new ChangeMotorSpeedEvent(mapToMotor(joystickEvent.getIndex()),
                  joystickEvent.getValue()));
         }
      }
   }

   int mapToMotor(int axis) {
      int ret;
      if(axisMotorMap.containsKey(axis)) {
         ret = axisMotorMap.get(axis);
      } else {
         throw new IllegalArgumentException("Unexpected axis " + axis);
      }
      return ret;
   }
}
