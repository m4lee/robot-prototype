package org.mib.robot.motor;

import com.github.oxo42.stateless4j.triggers.TriggerWithParameters1;

public class MotorSpeedUpdatedEventTrigger<S, T> extends TriggerWithParameters1<MotorSpeedUpdatedEvent, S, T> {
   public MotorSpeedUpdatedEventTrigger(T trigger) {
      super(trigger, MotorSpeedUpdatedEvent.class);
   }
}
