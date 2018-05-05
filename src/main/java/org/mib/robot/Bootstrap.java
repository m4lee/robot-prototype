package org.mib.robot;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import org.mib.robot.controller.ControllerSelectorService;
import org.mib.robot.controller.ManualControllerService;
import org.mib.robot.input.JoystickService;
import org.mib.robot.motor.MotorService;
import org.mib.robot.pi.GpioService;
import org.mib.robot.sensor.RangeFinderService;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public class Bootstrap {
   private static final Logger log = Logger.getLogger(Bootstrap.class.getName());

   @Inject
   GpioService gpio;

   @Inject
   JoystickService joystick;

   @Inject
   ControllerSelectorService selector;

   @Inject
   ManualControllerService manualController;

   @Inject
   MotorService motor;

   @Inject
   RangeFinderService rangeFinderService;

   private ServiceManager serviceManager;

   @Inject
   Bootstrap() {
      // allow injection
   }

   private static final long SERVICE_TIMEOUT = 5000; // ms

   void start() throws TimeoutException {
      assert gpio != null;
      assert joystick != null;
      assert selector != null;
      assert manualController != null;
      assert motor != null;
      assert rangeFinderService != null;
      assert serviceManager == null;

      addServiceLoggerListener(gpio, joystick, selector, manualController, motor,
            rangeFinderService);

      // start and stop the GPIO service separately to set ordering
      gpio.startAsync().awaitRunning(SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);

      serviceManager = new ServiceManager(Sets.newHashSet(joystick, manualController, selector,
            motor, rangeFinderService));

      // use a shutdown hook to shutdown the services
      Runtime.getRuntime().addShutdownHook(new Thread(Bootstrap.this::stop));

      serviceManager.startAsync().awaitHealthy(SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
   }

   private static class ServiceStatusLogger extends Service.Listener {
      private final Service service;

      public ServiceStatusLogger(Service service) {
         this.service = service;
      }

      @Override
      public void running() {
         log.info(service + " is running.");
      }

      @Override
      public void terminated(Service.State from) {
         log.info( service + " has stopped.");
      }
   }

   void addServiceLoggerListener(Service...services) {
      for(Service service : services) {
         service.addListener(new ServiceStatusLogger(service), MoreExecutors.directExecutor());
      }
   }

   void stop() {
      assert serviceManager != null;
      try {
         serviceManager.stopAsync();
         serviceManager.awaitStopped(SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
      } catch(TimeoutException te) {
         log.log(Level.SEVERE, "Timed out waiting for services to stop.", te);
      }

      try {
         gpio.stopAsync().awaitTerminated(SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
      } catch(TimeoutException te) {
         log.log(Level.SEVERE, "Timed out waiting for GPIO service to stop.", te);
      }
   }
}
