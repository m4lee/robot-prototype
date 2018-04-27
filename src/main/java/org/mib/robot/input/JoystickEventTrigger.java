package org.mib.robot.input;

import com.github.oxo42.stateless4j.triggers.TriggerWithParameters1;

public class JoystickEventTrigger<S, T> extends TriggerWithParameters1<JoystickEvent, S, T> {
   public JoystickEventTrigger(T trigger) {
      super(trigger, JoystickEvent.class);
   }
}
