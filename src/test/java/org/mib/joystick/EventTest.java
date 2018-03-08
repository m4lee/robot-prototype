package org.mib.joystick;


import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EventTest {

   /*
    * The event structure is
    * struct js_event {
    *    __u32 time;
    *    __s16 value;
    *    __u8 type;
    *    __u8 number;
    * };
    */
   @Test
   public void testConstructor() {
      byte[] testBuffer = new byte [] {
            0x04, 0x03, 0x02, 0x01, // time
            0x06, 0x05, // value
            0x01, // type
            0x07 // number
      };

      Event test = new Event(ByteBuffer.wrap(testBuffer).order(ByteOrder.LITTLE_ENDIAN));

      assertEquals("timestamp", 0x01020304, test.getTime());
      assertEquals("value", 0x0506, test.getValue());
      assertEquals( "type", Event.Type.BUTTON, test.getEventType());
      assertEquals("number", 0x07, test.getAxisOrButton());
      assertFalse( "init", test.isInit());

      byte[] initTestBuffer = new byte[] {
            0x04, 0x03, 0x02, 0x01, // time
            0x06, 0x05, // value
            (byte)0x82, // type
            0x07 // number
      };

      Event initTest = new Event(ByteBuffer.wrap(initTestBuffer).order(ByteOrder.LITTLE_ENDIAN));
      assertEquals("type", Event.Type.AXIS, initTest.getEventType());
      assertTrue("init", initTest.isInit());
   }
}