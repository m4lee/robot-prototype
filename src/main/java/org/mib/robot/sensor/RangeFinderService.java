package org.mib.robot.sensor;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractIdleService;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RangeFinderService extends AbstractIdleService {
   private static final Logger log = Logger.getLogger(RangeFinderService.class.getName());
   private static final String ID = "rangeFinder";
   private static final int MAX_READINGS = 10;

   private static class Reading {
      private final float reading;
      private final long time;

      private Reading(float reading, long time) {
         this.reading = reading;
         this.time = time;
      }

      private float getReading() {
         return reading;
      }

      @SuppressWarnings("unused")
      private long getTime() {
         return time;
      }
   }

   @Inject
   @SuppressWarnings("WeakerAccess")
   EventBus eventBus;

   @Inject
   @SuppressWarnings("WeakerAccess")
   Us100 distanceSensor;

   @Inject
   @SuppressWarnings("WeakerAccess")
   RangeFinderConfiguration configuration;

   @Inject
   @SuppressWarnings("WeakerAccess")
   public RangeFinderService() {
      // allow injection
   }

   @Override
   protected void startUp() throws Exception {
      distanceSensor.addHandler(new SensorEventHandler() {
         @Override
         public void onReading(float reading, long time) {
            eventBus.post(new SensorReadingEvent(ID, reading, time));
            poll();
         }

         @Override
         public void onBadReading(Object source) {
            eventBus.post(new SensorErrorEvent(ID));
            log.severe("Bad reading from " + source);
         }
      });

      distanceSensor.open(configuration.getUs100Configuration().getSerialDevicePath(),
            configuration.getUs100Configuration().getBaud(),
            configuration.getUs100Configuration().getDataBits(),
            configuration.getUs100Configuration().getParity(),
            configuration.getUs100Configuration().getStopBits(),
            configuration.getUs100Configuration().getFlowControl());

      poll();

   }

   void poll() {
      try {
         distanceSensor.triggerReading();
      } catch(Exception e) {
         log.log(Level.SEVERE, "Error triggering a reading.", e);
         eventBus.post(new SensorErrorEvent(ID));
      }
   }

   @Override
   protected void shutDown() throws Exception {
      distanceSensor.close();
   }
}
