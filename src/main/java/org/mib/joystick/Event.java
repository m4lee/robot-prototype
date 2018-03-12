package org.mib.joystick;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Event {
   private final long time; //time
   private final int value; // value
   private final Type eventType; //type
   private final int axisOrButton; // number
   private final boolean init;

   public static final int SIZE = 4 + 2 + 1 + 1; // time + value + type + number
   public static final int MAX_VALUE = Short.MAX_VALUE;
   public static final int MIN_VALUE = -Short.MAX_VALUE;

   public enum Type {
      BUTTON((byte)0x01),
      AXIS((byte)0x02),
      INIT((byte)0x80);

      private byte ordinal;
      private static final Map<Byte, Type> index = new HashMap<>();

      static {
         for(Type type : values()) {
            index.put(type.getOrdinal(), type);
         }
      }

      Type(byte ordinal) {
         this.ordinal = ordinal;
      }

      byte getOrdinal() {
         return ordinal;
      }

      static Type fromOrdinal(byte ordinal) {
         Type type = index.get(ordinal);
         if(type == null) {
            throw new IllegalArgumentException("Unexpected event type " + ordinal);
         }
         return index.get(ordinal);
      }
   }

   Event(ByteBuffer buffer) {
      this.time = Integer.toUnsignedLong(buffer.getInt());
      this.value = buffer.getShort();

      byte eventTypeCode = buffer.get();
      this.eventType = Type.fromOrdinal((byte)(eventTypeCode & ~Type.INIT.getOrdinal()));
      this.init = (eventTypeCode & Type.INIT.getOrdinal()) == Type.INIT.getOrdinal();

      this.axisOrButton = Byte.toUnsignedInt(buffer.get());
   }

   public long getTime() {
      return time;
   }

   public int getValue() {
      return value;
   }

   public Type getEventType() {
      return eventType;
   }

   public int getAxisOrButton() {
      return axisOrButton;
   }

   public boolean isInit() {
      return init;
   }

   @Override
   public String toString() {
      return "Event (time: " + getTime() +
            ",value: " + getValue() +
            ",type: " + getEventType() +
            ",number: " + getAxisOrButton() +
            ",init: " + isInit() + ")";
   }
}
