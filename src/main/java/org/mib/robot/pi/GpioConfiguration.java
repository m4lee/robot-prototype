package org.mib.robot.pi;

public class GpioConfiguration {
   private int clockDivisor;
   private String pwmMode;

   public int getClockDivisor() {
      return clockDivisor;
   }

   @SuppressWarnings("unused")
   public void setClockDivisor(int clockDivisor) {
      this.clockDivisor = clockDivisor;
   }

   public String getPwmMode() {
      return pwmMode;
   }

   @SuppressWarnings("unused")
   public void setPwmMode(String pwmMode) {
      this.pwmMode = pwmMode;
   }
}
