package org.mib.robot.motor;

import java.util.Collection;

public class MotorConfiguration {
   private int pwmPeriod;
   private Collection<Instance> instance;

   public int getPwmPeriod() {
      return pwmPeriod;
   }

   public void setPwmPeriod(int pwmPeriod) {
      this.pwmPeriod = pwmPeriod;
   }

   public Collection<Instance> getInstance() {
      return instance;
   }

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

      public void setIndex(int index) {
         this.index = index;
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public int getEnablePin() {
         return enablePin;
      }

      public void setEnablePin(int enablePin) {
         this.enablePin = enablePin;
      }

      public int getDirectionPin() {
         return directionPin;
      }

      public void setDirectionPin(int directionPin) {
         this.directionPin = directionPin;
      }

      public boolean isInvert() {
         return invert;
      }

      public void setInvert(boolean invert) {
         this.invert = invert;
      }
   }
}
