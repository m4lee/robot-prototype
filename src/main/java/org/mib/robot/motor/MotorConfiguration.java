package org.mib.robot.motor;

import java.util.Collection;

class MotorConfiguration {
   private int pwmPeriod;
   private Collection<Instance> instance;

   public int getPwmPeriod() {
      return pwmPeriod;
   }

   @SuppressWarnings("unused")
   public void setPwmPeriod(int pwmPeriod) {
      this.pwmPeriod = pwmPeriod;
   }

   public Collection<Instance> getInstance() {
      return instance;
   }

   @SuppressWarnings("unused")
   public void setInstance(Collection<Instance> instance) {
      this.instance = instance;
   }

   public static class Instance {
      private int index;
      private String name;
      private int enablePin;
      private int directionPin;
      private boolean invert;

      public int getIndex() {
         return index;
      }

      @SuppressWarnings("unused")
      public void setIndex(int index) {
         this.index = index;
      }

      public String getName() {
         return name;
      }

      @SuppressWarnings("unused")
      public void setName(String name) {
         this.name = name;
      }

      public int getEnablePin() {
         return enablePin;
      }

      @SuppressWarnings("unused")
      public void setEnablePin(int enablePin) {
         this.enablePin = enablePin;
      }

      public int getDirectionPin() {
         return directionPin;
      }

      @SuppressWarnings("unused")
      public void setDirectionPin(int directionPin) {
         this.directionPin = directionPin;
      }

      public boolean isInvert() {
         return invert;
      }

      @SuppressWarnings("unused")
      public void setInvert(boolean invert) {
         this.invert = invert;
      }
   }
}
