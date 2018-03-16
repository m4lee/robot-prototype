package org.mib.robot.input;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractIdleService;
import org.mib.joystick.Joystick;

import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JoystickService is an output service which raises @link {JoystickEvent JoystickEvent's} to
 * the provided EventBus.
 */
public class JoystickService extends AbstractIdleService {
   private static final Logger log = Logger.getLogger(JoystickService.class.getName());

   @SuppressWarnings("WeakerAccess")
   @Inject EventBus eventBus;
   @SuppressWarnings("WeakerAccess")
   @Inject Joystick joystick;

   @Inject
   JoystickService() {
      // allow injection
   }

   @Override
   protected void startUp() {
      joystick.addHandler(event -> eventBus.post(translateEvent(event)));
      try {
         joystick.open();
      } catch(IOException ioe) {
         log.log(Level.SEVERE, "Could not open joystick device.", ioe);
      }
   }

   JoystickEvent translateEvent(org.mib.joystick.Event apiEvent) {
      assert apiEvent != null;

      JoystickEvent event;
      if(apiEvent.getEventType() == org.mib.joystick.Event.Type.AXIS) {
         event = new JoystickEvent(apiEvent.getAxisOrButton(), (float)apiEvent.getValue() /
               (float) org.mib.joystick.Event.MAX_VALUE);
      } else {
         event = new JoystickEvent(apiEvent.getAxisOrButton(), apiEvent.getValue() != 0);
      }
      return event;
   }

   @Override
   protected void shutDown() {
      joystick.close();
   }
}
