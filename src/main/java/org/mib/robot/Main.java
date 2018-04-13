package org.mib.robot;

@SuppressWarnings("WeakerAccess")
public class Main {
   public static void main(String[] args) throws Exception {
      PBot.Builder builder = DaggerPBot.builder();
      if(args.length > 0) {
         builder.configurationFile(args[1]);
      }
      builder.build().bootstrap().start();
   }
}
