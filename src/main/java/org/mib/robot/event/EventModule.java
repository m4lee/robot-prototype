package org.mib.robot.event;

import com.google.common.eventbus.EventBus;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class EventModule {
   @Provides @Singleton
   public static EventBus eventBus() {
      return new EventBus();
   }
}
