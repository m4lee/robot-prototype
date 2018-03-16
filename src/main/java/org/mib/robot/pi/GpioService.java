package org.mib.robot.pi;

import com.google.common.util.concurrent.AbstractIdleService;
import com.pi4j.io.gpio.GpioController;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * This service just exists to make sure that the GPIO gets shutdown
 */
public class GpioService extends AbstractIdleService {
   private static final Logger log = Logger.getLogger(GpioService.class.getName());

   @Inject
   @SuppressWarnings("WeakerAccess")
   GpioController gpio;

   @Inject
   @SuppressWarnings("WeakerAccess")
   GpioService() {
      // allow injection
   }

   @Override
   protected void startUp() {
      assert gpio != null && !gpio.isShutdown();
   }

   @Override
   protected void shutDown() {
      assert !gpio.isShutdown();

      if(gpio.isShutdown()) {
         log.warning("GPIO controller already closed when trying to shut down.");
      }
      gpio.shutdown();
   }
}
