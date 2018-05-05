package org.mib.robot.controller;

import java.util.Map;

class SelectorConfiguration {
   private Map<Integer, String> buttonControllerMap;
   private String defaultController;

   public Map<Integer, String> getButtonControllerMap() {
      return buttonControllerMap;
   }

   @SuppressWarnings("unused")
   public void setButtonControllerMap(Map<Integer, String> buttonControllerMap) {
      this.buttonControllerMap = buttonControllerMap;
   }

   public String getDefaultController() {
      return defaultController;
   }

   public void setDefaultController(String defaultController) {
      this.defaultController = defaultController;
   }
}
