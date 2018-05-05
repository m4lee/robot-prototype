package org.mib.robot.controller;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Func2;
import com.google.common.eventbus.Subscribe;
import org.mib.robot.event.Event;
import org.mib.robot.event.EventQueueService;
import org.mib.robot.input.JoystickEvent;
import org.mib.robot.input.JoystickEventTrigger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ControllerSelectorService extends EventQueueService {
   public static final String ID = "selector";

   enum State { INIT, STARTING, LISTENING, STOPPING_CURRENT, STARTING_NEW }
   enum Trigger { START, SWITCH, CONTROLLER_STARTED, CONTROLLER_STOPPED }

   private static final ControllerControlEventTrigger<State, Trigger> STOPPED_TRIGGER =
         new ControllerControlEventTrigger<>(Trigger.CONTROLLER_STOPPED);
   private static final ControllerControlEventTrigger<State, Trigger> START_TRIGGER =
         new ControllerControlEventTrigger<>(Trigger.CONTROLLER_STARTED);
   private static final JoystickEventTrigger<State, Trigger> SWITCH_TRIGGER =
         new JoystickEventTrigger<>(Trigger.SWITCH);

   private final Map<Integer, String> buttonControllerMap;
   private String activeController;
   private String pendingController;
   private final StateMachine<State, Trigger> stateMachine;

   @Inject
   ControllerSelectorService(SelectorConfiguration configuration) {
      this.buttonControllerMap = Collections.unmodifiableMap(
            configuration.getButtonControllerMap());
      this.pendingController = configuration.getDefaultController();

      StateMachineConfig<State, Trigger> stateMachineConfig = new StateMachineConfig<>();

      stateMachineConfig.configure(State.INIT)
            .permit(Trigger.START, State.STARTING, this::startDefaultController);

      stateMachineConfig.configure(State.STARTING)
            .permitDynamic(START_TRIGGER, handleStartedTrigger(State.STARTING));

      stateMachineConfig.configure(State.LISTENING)
            .onEntry(this::trackControllers)
            .permitDynamic(SWITCH_TRIGGER, this::handleSwitchTrigger);

      stateMachineConfig.configure(State.STOPPING_CURRENT)
            .onEntryFrom(SWITCH_TRIGGER, this::stopActiveController, JoystickEvent.class)
            .permitDynamic(STOPPED_TRIGGER, this::handleStoppedTrigger);

      stateMachineConfig.configure(State.STARTING_NEW)
            .onEntryFrom(STOPPED_TRIGGER, this::startPendingController, ControllerControlEvent
                  .class)
            .permitDynamic(START_TRIGGER, handleStartedTrigger(State.STARTING_NEW));

      stateMachine = new StateMachine<>(State.INIT, stateMachineConfig);
      stateMachine.onUnhandledTrigger((s, t) -> {});
   }

   @Subscribe
   public void onControllerControlEvent(ControllerControlEvent event) {
      queue.add(event);
   }

   @Subscribe
   public void onJoystickEvent(JoystickEvent event) {
      queue.add(event);
   }

   @Override
   protected void startUp() {
      getEventBus().register(this);
      stateMachine.fire(Trigger.START);
   }

   @Override
   protected void handleEvent(Event event) {
      if(event instanceof JoystickEvent) {
         stateMachine.fire(SWITCH_TRIGGER, (JoystickEvent)event);
      } else if(event instanceof ControllerControlEvent) {
         ControllerControlEvent controlEvent = (ControllerControlEvent)event;
         if(controlEvent.getInstruction() == ControllerControlEvent.Instruction.STARTED) {
            stateMachine.fire(START_TRIGGER, controlEvent);
         } else if (controlEvent.getInstruction() == ControllerControlEvent.Instruction.STOPPED) {
            stateMachine.fire(STOPPED_TRIGGER, controlEvent);
         }
      }
   }

   private void startDefaultController() {
      getEventBus().post(new ControllerControlEvent(ControllerControlEvent.Instruction.START,
            pendingController));
   }

   private void trackControllers() {
      activeController = pendingController;
      pendingController = null;
   }

   private State handleSwitchTrigger(JoystickEvent event) {
      State next = State.LISTENING;
      if(event.getType() == JoystickEvent.Type.Button && event.isPressed())  {
         String nextId = buttonControllerMap.get(event.getIndex());
         if(nextId != null && !Objects.equals(nextId, activeController)) {
            next = State.STOPPING_CURRENT;
         }
      }
      return next;
   }

   private void stopActiveController(JoystickEvent event) {
      pendingController = buttonControllerMap.get(event.getIndex());
      getEventBus().post(new ControllerControlEvent(ControllerControlEvent.Instruction.STOP,
            activeController));
   }

   private State handleStoppedTrigger(ControllerControlEvent event) {
      return Objects.equals(activeController, event.getId()) ? State.STARTING_NEW :
            State.STOPPING_CURRENT;
   }

   private void startPendingController(@SuppressWarnings("unused") ControllerControlEvent event) {
      getEventBus().post(new ControllerControlEvent(ControllerControlEvent.Instruction.START,
            pendingController));
   }

   private Func2<ControllerControlEvent, State> handleStartedTrigger(State self) {
      return (ControllerControlEvent event) -> Objects.equals(pendingController, event.getId()) ? State.LISTENING : self;
   }
}
