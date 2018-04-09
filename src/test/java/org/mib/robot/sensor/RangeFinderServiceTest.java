package org.mib.robot.sensor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
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
      doCallRealMethod().when(component.sensor()).addHandler(any());
      doCallRealMethod().when(component.sensor()).raiseReading(anyFloat(), anyLong());

      RangeFinderService service = component.rangeFinderService();
      try {
         service.startAsync().awaitRunning(2000, TimeUnit.MILLISECONDS);
         component.sensor().raiseReading(1f, 0);
         component.sensor().raiseReading(2f, 0);
         component.sensor().raiseReading(3f, 0);

         assertEquals("Wrong average captured.", 2f, service.getLastAverage(), 0f);
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

   @SuppressWarnings("WeakerAccess")
   @Module
   static class MockUs100Module {
      @Provides
      @Singleton
      static Us100 us100() {
         return mock(Us100.class, withSettings().useConstructor());
      }
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