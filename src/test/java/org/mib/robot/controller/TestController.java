package org.mib.robot.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class TestController {
   private final String id;
   private EventBus eventBus;
   private boolean started = false;
   private final ReentrantLock lock = new ReentrantLock();
   private final Condition condition = lock.newCondition();

   TestController(String id) {
      this.id = id;
   }

   @Subscribe
   public void onControllerControlEvent(ControllerControlEvent event) {
      if(Objects.equals(id, event.getId())) {
         lock.lock();
         ControllerControlEvent.Instruction instruction = null;
         try {
            switch (event.getInstruction()) {
               case START:
                  instruction = ControllerControlEvent.Instruction.STARTED;
                  started = true;
                  break;
               case STOP:
                  instruction = ControllerControlEvent.Instruction.STOPPED;
                  started = false;
                  break;
            }
            condition.signalAll();
         } finally {
            lock.unlock();
         }
         if(instruction != null) {
            eventBus.post(new ControllerControlEvent(instruction, id));
         }
      }
   }

   void startUp(EventBus eventBus) {
      this.eventBus = eventBus;
      eventBus.register(this);
   }

   boolean waitStarted(boolean started, long time, @SuppressWarnings("SameParameterValue") TimeUnit unit)  throws InterruptedException {
      boolean expired = false;
      lock.lock();
      long end = System.currentTimeMillis() + unit.toMillis(time);
      try {
         while(this.started != started && !expired) {
            long remaining = end - System.currentTimeMillis();
            if(remaining > 0) {
               condition.await(remaining, unit);
            } else {
               expired = true;
            }
         }
      } finally {
         lock.unlock();
      }
      return !expired;
   }
}
