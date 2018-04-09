package org.mib.robot.sensor;

interface SensorEventHandler {
   void onReading(float reading, long time);
   void onBadReading(Object source);
}
