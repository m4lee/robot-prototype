package org.mib.robot.sensor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;
import org.mib.robot.configuration.TestConfigurationModule;
import org.mib.robot.event.EventModule;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class RangeFinderServiceTest {

   private TestSensors component;

   @Before
   public void before() {
      component = DaggerRangeFinderServiceTest_TestSensors.create();
   }

   @Test
   public void testStartUp() throws Exception {
      RangeFinderService service = component.rangeFinderService();
      try {
         service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);

         verify(component.sensor()).open(any(), anyInt(), anyInt(), any(), anyInt(), any());

         Thread.sleep(250);
         verify(component.sensor(), atLeastOnce()).triggerReading();
      } finally {
         service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);
      }
   }

   @Test
   public void testPoll() throws Exception {
      RangeFinderService service = component.rangeFinderService();
      service.poll();

      verify(component.sensor()).triggerReading();

      doThrow(new IOException()).when(component.sensor()).triggerReading();
      service.poll();
   }

   @Test
   public void testSensorError() throws Exception {
      class SensorErrorHandler {
         private SensorErrorEvent event;
         private final CountDownLatch latch = new CountDownLatch(1);
         @Subscribe
         public void onSensorError(SensorErrorEvent event) {
            this.event = event;
            latch.countDown();
         }
      }

      doCallRealMethod().when(component.sensor()).addHandler(any());
      doCallRealMethod().when(component.sensor()).raiseError();

      SensorErrorHandler errorHandler = new SensorErrorHandler();
      component.eventBus().register(errorHandler);
      RangeFinderService service = component.rangeFinderService();
      try {
         service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);
         component.sensor().raiseError();
         errorHandler.latch.await(2000, TimeUnit.MILLISECONDS);
         assertNotNull("Error event not raised", errorHandler.event);
      } finally {
         service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);
      }
   }

   @Test
   public void testReadings() throws Exception {
      class SensorReadingHandler {
         private SensorReadingEvent event;
         private final CountDownLatch latch = new CountDownLatch(1);
         @Subscribe
         public void onSensorError(SensorReadingEvent event) {
            this.event = event;
            latch.countDown();
         }
      }
      doCallRealMethod().when(component.sensor()).addHandler(any());
      doCallRealMethod().when(component.sensor()).raiseReading(anyFloat(), anyLong());

      SensorReadingHandler handler = new SensorReadingHandler();
      component.eventBus().register(handler);

      RangeFinderService service = component.rangeFinderService();
      try {
         service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);
         component.sensor().raiseReading(1f, 0);

         handler.latch.await(2000, TimeUnit.MILLISECONDS);
         assertNotNull("Reading event not raised", handler.event);
      } finally {
         service.stopAsync().awaitTerminated(2000, TimeUnit.MILLISECONDS);
      }
   }

   @Test
   public void testShutDown() throws Exception {
      RangeFinderService service = component.rangeFinderService();
      service.shutDown();
      verify(component.sensor()).close();
   }

   @Component(modules={MockUs100Module.class, TestConfigurationModule.class, SensorModule.class,
         EventModule.class})
   @Singleton
   interface TestSensors {
      RangeFinderService rangeFinderService();
      Us100 sensor();
      EventBus eventBus();
   }
}