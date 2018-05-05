package org.mib.robot.controller;

import com.github.oxo42.stateless4j.triggers.TriggerWithParameters1;

class ControllerControlEventTrigger<S, T> extends
      TriggerWithParameters1<ControllerControlEvent, S, T> {

   public ControllerControlEventTrigger(T trigger) {
      super(trigger, ControllerControlEvent.class);
   }
}
