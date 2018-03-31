package org.mib.robot.controller;

import java.util.Map;

public class ControllerConfiguration {
   private Map<Integer, Integer> axisMotorMap;

   public Map<Integer, Integer> getAxisMotorMap() {
      return axisMotorMap;
   }

   public void setAxisMotorMap(Map<Integer, Integer> axisMotorMap) {
      this.axisMotorMap = axisMotorMap;
   }
}
