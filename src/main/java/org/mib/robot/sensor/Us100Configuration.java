package org.mib.robot.sensor;

@SuppressWarnings("ALL")
class Us100Configuration {
   private String serialDevicePath;
   private int baud;
   private int dataBits;
   private String parity;
   private int stopBits;
   private String flowControl;

   public String getSerialDevicePath() {
      return serialDevicePath;
   }

   public void setSerialDevicePath(String serialDevicePath) {
      this.serialDevicePath = serialDevicePath;
   }

   public int getBaud() {
      return baud;
   }

   @SuppressWarnings("unused")
   public void setBaud(int baud) {
      this.baud = baud;
   }

   public int getDataBits() {
      return dataBits;
   }

   @SuppressWarnings("unused")
   public void setDataBits(int dataBits) {
      this.dataBits = dataBits;
   }

   public String getParity() {
      return parity;
   }

   @SuppressWarnings("unused")
   public void setParity(String parity) {
      this.parity = parity;
   }

   public int getStopBits() {
      return stopBits;
   }

   @SuppressWarnings("unused")
   public void setStopBits(int stopBits) {
      this.stopBits = stopBits;
   }

   public String getFlowControl() {
      return flowControl;
   }

   @SuppressWarnings("unused")
   public void setFlowControl(String flowControl) {
      this.flowControl = flowControl;
   }
}
