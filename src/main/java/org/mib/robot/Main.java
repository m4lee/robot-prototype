package org.mib.robot;

@SuppressWarnings("WeakerAccess")
public class Main {
   public static void main(String[] args) throws Exception {
      Bootstrap bootstrap = DaggerPBot.create().bootstrap();
      bootstrap.start();
   }
}
