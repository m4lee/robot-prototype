package org.mib.robot.controller;

import org.mib.robot.event.Event;

class ControllerControlEvent extends Event {
   enum Instruction { START, STOP, STOPPED, STARTED }

   private final Instruction instruction;
   private final String id;

   ControllerControlEvent(Instruction instruction, String id) {
      this.instruction = instruction;
      this.id = id;
   }

   Instruction getInstruction() {
      return instruction;
   }

   String getId() {
      return id;
   }
}
