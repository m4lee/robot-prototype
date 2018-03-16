package org.mib.robot;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ServiceManager;
import org.mib.robot.controller.ControllerService;
import org.mib.robot.input.JoystickService;
import org.mib.robot.motor.MotorService;
import org.mib.robot.pi.GpioService;

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
   ControllerService controller;

   @Inject
   MotorService motor;

   private ServiceManager serviceManager;

   @Inject
   Bootstrap() {
      // allow injection
   }

   private static final long SERVICE_TIMEOUT = 5000; // ms

   void start() throws TimeoutException {
      assert gpio != null;
      assert joystick != null;
      assert controller != null;
      assert motor != null;
      assert serviceManager == null;

      // start and stop the GPIO service separately to set ordering
      gpio.startAsync().awaitRunning(SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);

      serviceManager = new ServiceManager(Sets.newHashSet(joystick, controller,
            motor));

      // use a shutdown hook to shutdown the services
      Runtime.getRuntime().addShutdownHook(new Thread(Bootstrap.this::stop));

      serviceManager.startAsync().awaitHealthy(SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
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
