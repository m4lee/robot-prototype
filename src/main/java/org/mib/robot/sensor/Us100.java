package org.mib.robot.sensor;

import com.pi4j.io.serial.*;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Us100 implements AutoCloseable {
   private static final Logger log = Logger.getLogger(Us100.class.getName());
   private static final int READING_LENGTH = 2;

   enum SensorState {
      IDLE, COMMAND_SENT
   }

   private final Serial serial = SerialFactory.createInstance();
   private volatile SensorState state = SensorState.IDLE;
   private final ArrayList<SensorEventHandler> handlers = new ArrayList<>();

   @Inject
   Us100() {
      this.serial.addListener((SerialDataEventListener) event -> {
         try {
            long time = System.nanoTime();
            ByteBuffer receivedData = event.getByteBuffer();
            if(state == SensorState.COMMAND_SENT && receivedData.remaining() == READING_LENGTH) {
               raiseReading(Short.toUnsignedInt(receivedData.getShort()), time);
            } else {
               raiseError();
            }
            state = SensorState.IDLE;
         } catch(IOException ioe) {
            log.log(Level.SEVERE, "Error reading distance.", ioe);
         }
      });
   }

   void addHandler(SensorEventHandler handler) {
      handlers.add(handler);
   }

   void open(String port, int baud, int dataBits, String parity, int stopBits, String flowControl)
      throws IOException {
      Baud baudParam = Baud.getInstance(baud);
      DataBits dataBitsParam = DataBits.getInstance(dataBits);
      Parity parityParam = Parity.getInstance(parity);
      StopBits stopBitsParam = StopBits.getInstance(stopBits);
      FlowControl flowPControlParam = FlowControl.getInstance(flowControl);

      if(baudParam == null) {
         throw new IllegalArgumentException("Invalid baud rate specified: " + baud + ".");
      }

      if(dataBitsParam == null) {
         throw new IllegalArgumentException("Invalid number of data bis specified: " + dataBits
               +".");
      }

      if(parityParam == null) {
         throw new IllegalArgumentException("Invalid parity specified: " + parity + ".");
      }

      if(stopBitsParam == null) {
         throw new IllegalArgumentException("Invalid number of stop bits specified: " + stopBits
               + ".");
      }

      if(flowPControlParam == null) {
         throw new IllegalArgumentException("Invalid flow control specified: " + flowControl + ".");
      }

      serial.open(port, baudParam, dataBitsParam, parityParam, stopBitsParam, flowPControlParam);
   }

   @SuppressWarnings("WeakerAccess")
   void raiseReading(float reading, long time) {
      handlers.forEach(h -> {
         if (h != null) {
            try {
               h.onReading(reading, time);
            } catch (Exception e) {
               log.log(Level.SEVERE, "Error handling sensor data ready event.", e);
            }
         }
      });
   }

   @SuppressWarnings("WeakerAccess")
   void raiseError() {
      handlers.forEach(h -> {
         if (h != null) {
            try {
               h.onBadReading(this);
            } catch (Exception e) {
               log.log(Level.SEVERE, "Error handling bad reading event.", e);
            }
         }
      });
   }

   void triggerReading() throws IOException {
      serial.getOutputStream().write(new byte[] { 0x55 });
      state = SensorState.COMMAND_SENT;
   }

   @Override
   public void close() throws Exception {
      serial.close();
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }
}
