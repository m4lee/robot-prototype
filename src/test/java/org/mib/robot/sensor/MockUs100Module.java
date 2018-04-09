package org.mib.robot.sensor;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("WeakerAccess")
@Module
public class MockUs100Module {
   @Provides
   @Singleton
   static Us100 us100() {
      return mock(Us100.class, withSettings().useConstructor());
   }
}
