package org.mib.robot.controller;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Action;
import com.google.common.eventbus.Subscribe;
import org.mib.robot.event.Event;
import org.mib.robot.event.EventQueueService;
import org.mib.robot.input.JoystickEvent;
import org.mib.robot.input.JoystickEventTrigger;
import org.mib.robot.motor.ChangeMotorSpeedEvent;
import org.mib.robot.motor.MotorSpeedUpdatedEvent;
import org.mib.robot.motor.MotorSpeedUpdatedEventTrigger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ManualControllerService extends EventQueueService {
   public static final String ID = "manualController";

   enum State { INIT, CHANGING_SPEEDS, PAUSING, PAUSED }
   enum Trigger { START, MANUAL_INPUT, PAUSE, SPEED_CHANGED, RESUME }

   private static final JoystickEventTrigger<State, Trigger> JOYSTICK_EVENT_TRIGGER =
         new JoystickEventTrigger<>(Trigger.MANUAL_INPUT);
   private static final MotorSpeedUpdatedEventTrigger<State, Trigger> MOTOR_SPEED_UPDATED_EVENT_TRIGGER =
         new MotorSpeedUpdatedEventTrigger<>(Trigger.SPEED_CHANGED);

   private final Map<Integer, Integer> axisMotorMap;
   private final Map<Integer, AtomicInteger> pendingSpeedChanges;
   private final StateMachine<State, Trigger> stateMachine;


   @Inject
   ManualControllerService(ControllerConfiguration configuration) {
      axisMotorMap = Collections.unmodifiableMap(configuration.getAxisMotorMap());
      pendingSpeedChanges = axisMotorMap.values().stream().collect(Collectors.toMap(
            Function.identity(), i -> new AtomicInteger(0)));

      StateMachineConfig<State, Trigger> stateMachineConfig = new StateMachineConfig<>();

      stateMachineConfig.configure(State.INIT)
            .permit(Trigger.START, State.PAUSED);

      stateMachineConfig.configure(State.PAUSED)
            .onEntry(postControlProcessed(ControllerControlEvent.Instruction.STOPPED))
            .permit(Trigger.RESUME, State.CHANGING_SPEEDS);

      stateMachineConfig.configure(State.CHANGING_SPEEDS)
            .onEntry(this::resetPendingMap)
            .onEntry(postControlProcessed(ControllerControlEvent.Instruction.STARTED))
            .permitDynamic(JOYSTICK_EVENT_TRIGGER, this::changingSpeeds, this::handleJoystickEvent)
            .permitDynamic(MOTOR_SPEED_UPDATED_EVENT_TRIGGER, this::changingSpeeds,
                  this::decrementPendingSpeedUpdate)
            .permit(Trigger.PAUSE, State.PAUSING, this::stopAllMotors);

      stateMachineConfig.configure(State.PAUSING)
            .permitDynamic(MOTOR_SPEED_UPDATED_EVENT_TRIGGER,
                  this::pausedWhenAllMotorsStopped, this::raiseStoppedEvent);


      stateMachine = new StateMachine<>(State.INIT, stateMachineConfig);
      stateMachine.onUnhandledTrigger((s, t) -> {}); // allow unexpected triggers
   }

   @Subscribe
   public void onJoystickEvent(JoystickEvent event) {
      queue.add(event);
   }

   @Subscribe
   public void onControllerControlEvent(ControllerControlEvent event) {
      queue.add(event);
   }

   @Subscribe
   public void onMotorSpeedUpdatedEvent(MotorSpeedUpdatedEvent event) {
      queue.add(event);
   }

   @Override
   public void startUp() {
      stateMachine.fire(Trigger.START);
      getEventBus().register(this);
   }

   @Override
   protected void handleEvent(Event event) {
      if(event instanceof JoystickEvent) {
         stateMachine.fire(JOYSTICK_EVENT_TRIGGER, (JoystickEvent)event);
      } else if (event instanceof ControllerControlEvent) {
         ControllerControlEvent controlEvent = (ControllerControlEvent)event;
         switch(controlEvent.getInstruction()) {
            case START:
               stateMachine.fire(Trigger.RESUME);
               break;
            case STOP:
               stateMachine.fire(Trigger.PAUSE);
               break;
         }
      } else if (event instanceof MotorSpeedUpdatedEvent) {
         stateMachine.fire(MOTOR_SPEED_UPDATED_EVENT_TRIGGER, (MotorSpeedUpdatedEvent)event);
      }
   }

   @SuppressWarnings({"SameReturnValue", "unused"})
   private <T> State changingSpeeds(T trigger) {
      return State.CHANGING_SPEEDS;
   }

   private void decrementPendingSpeedUpdate(MotorSpeedUpdatedEvent event) {
      pendingSpeedChanges.get(event.getMotor()).decrementAndGet();
   }

   private State pausedWhenAllMotorsStopped(MotorSpeedUpdatedEvent event) {
      decrementPendingSpeedUpdate(event);
      State ret = State.PAUSING;
      if(pendingSpeedChanges.values().stream()
            .map(AtomicInteger::get).allMatch(Predicate.isEqual(0))) {
         ret = State.PAUSED;
      }
      return ret;
   }

   @SuppressWarnings("unused")
   private void raiseStoppedEvent(MotorSpeedUpdatedEvent event) {
      getEventBus().post(new ControllerControlEvent(ControllerControlEvent.Instruction.STOPPED,
            ID));
   }

   private void handleJoystickEvent(JoystickEvent event) {
      if (event.getType() == JoystickEvent.Type.Axis) {
         // translate the axis to a motor
         postMotorSpeed(mapToMotor(event.getIndex()), -event.getValue());
      }
   }

   private void postMotorSpeed(int motor, float value) {
      getEventBus().post(new ChangeMotorSpeedEvent(motor, value));
      pendingSpeedChanges.get(motor).incrementAndGet();
   }

   private void stopAllMotors() {
      axisMotorMap.values().forEach(m -> postMotorSpeed(m, 0));
   }

   private void resetPendingMap() {
      pendingSpeedChanges.values().forEach(a -> a.set(0));
   }

   private Action postControlProcessed(ControllerControlEvent.Instruction instruction) {
      return () -> getEventBus().post(new ControllerControlEvent(instruction, ManualControllerService.ID));
   }

   int mapToMotor(int axis) {
      int ret;
      if(axisMotorMap.containsKey(axis)) {
         ret = axisMotorMap.get(axis);
      } else {
         throw new IllegalArgumentException("Unexpected axis " + axis);
      }
      return ret;
   }
}
