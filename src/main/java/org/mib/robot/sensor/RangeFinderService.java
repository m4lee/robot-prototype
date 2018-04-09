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

   private final ScheduledExecutorService pollingExecutor = Executors.newScheduledThreadPool(1);
   private final Deque<Reading> readings = new ArrayDeque<>(MAX_READINGS);

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
            log.finest("Distance reading: " + reading);
            synchronized(readings) {
               readings.addFirst(new Reading(reading, time));
               if (readings.size() > MAX_READINGS) {
                  readings.removeLast();
               }
            }
         }

         @Override
         public void onBadReading(Object source) {
            eventBus.post(new SensorErrorEvent(source));
            log.severe("Bad reading from " + source);
         }
      });

      distanceSensor.open(configuration.getUs100Configuration().getSerialDevicePath(),
            configuration.getUs100Configuration().getBaud(),
            configuration.getUs100Configuration().getDataBits(),
            configuration.getUs100Configuration().getParity(),
            configuration.getUs100Configuration().getStopBits(),
            configuration.getUs100Configuration().getFlowControl());

      pollingExecutor.scheduleAtFixedRate(this::poll, 0, configuration.getPollPeriod(),
            TimeUnit.MILLISECONDS);
   }

   void poll() {
      try {
         distanceSensor.triggerReading();
      } catch(Exception e) {
         log.log(Level.SEVERE, "Error triggering a reading.", e);
      }
   }

   @Override
   protected void shutDown() throws Exception {
      try {
         pollingExecutor.shutdown();
         pollingExecutor.awaitTermination(2000, TimeUnit.MILLISECONDS);
      } catch(Exception e) {
         log.log(Level.SEVERE, "Error while closing range finder.", e);
      }
      distanceSensor.close();
   }

   public float getLastAverage() {
      synchronized(readings) {
         return readings.stream().collect(Collectors.averagingDouble(Reading::getReading))
               .floatValue();
      }
   }
}
