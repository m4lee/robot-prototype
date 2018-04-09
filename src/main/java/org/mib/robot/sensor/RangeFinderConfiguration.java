package org.mib.robot.sensor;

class RangeFinderConfiguration {
   private long pollPeriod;
   private Us100Configuration us100Configuration;

   public long getPollPeriod() {
      return pollPeriod;
   }

   @SuppressWarnings("unused")
   public void setPollPeriod(long pollPeriod) {
      this.pollPeriod = pollPeriod;
   }

   public Us100Configuration getUs100Configuration() {
      return us100Configuration;
   }

   @SuppressWarnings("unused")
   public void setUs100Configuration(Us100Configuration us100Configuration) {
      this.us100Configuration = us100Configuration;
   }
}
