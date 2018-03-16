package org.mib.robot.event;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

import javax.inject.Inject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class EventQueueService extends AbstractExecutionThreadService {
   private static final Logger log = Logger.getLogger(EventQueueService.class.getName());

   @Inject
   @SuppressWarnings("WeakerAccess")
   EventBus eventBus;

   private static final int MAX_LENGTH = 10000; // Add a bound for some safety

   protected final BlockingQueue<Event> queue = new LinkedBlockingQueue<>(MAX_LENGTH);

   protected EventBus getEventBus() {
      return eventBus;
   }

   private final static Event STOP_EVENT = new Event();

   @Override
   public void run() {

      while(isRunning()) {
         try {
            Event event = queue.take();
            if(event != STOP_EVENT) {
               handleEvent(event);
            } else {
               log.info("Stopping " + serviceName() + ".");
            }
         } catch(InterruptedException ie) {
            log.info("Queue read for " + serviceName() + "interrupted.");
         } catch(Exception e) {
            log.log(Level.SEVERE, "Error while handling event.", e);
         }
      }
   }

   @Override
   protected void triggerShutdown() {
      queue.add(STOP_EVENT);
   }

   protected abstract void handleEvent(Event event);
}
