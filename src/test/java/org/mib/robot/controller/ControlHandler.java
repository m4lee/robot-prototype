package org.mib.robot.controller;

import com.google.common.eventbus.Subscribe;

import java.util.concurrent.CountDownLatch;

class ControlHandler {
   private static final int EVENT_COUNT = 2;
   public ControllerControlEvent captured;
   public CountDownLatch latch = new CountDownLatch(EVENT_COUNT);
   @Subscribe
   public void onControllerControlEvent(ControllerControlEvent event) {
      this.captured = event;
      latch.countDown();
   }

   public void reset() {
      captured = null;
      latch = new CountDownLatch(EVENT_COUNT);
   }
}
