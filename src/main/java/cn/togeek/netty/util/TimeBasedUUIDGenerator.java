package cn.togeek.netty.util;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

class TimeBasedUUIDGenerator implements UUIDGenerator {
   // We only use bottom 3 bytes for the sequence number. Paranoia: init with
   // random int so that if JVM/OS/machine goes down, clock slips
   // backwards, and JVM comes back up, we are less likely to be on the same
   // sequenceNumber at the same time:
   private final AtomicInteger sequenceNumber =
      new AtomicInteger(SecureRandomHolder.INSTANCE.nextInt());

   // Used to ensure clock moves forward:
   private long lastTimestamp;

   private static final byte[] secureMungedAddress =
      MacAddressProvider.getSecureMungedAddress();

   static {
      assert secureMungedAddress.length == 6;
   }

   /**
    * Puts the lower numberOfLongBytes from l into the array, starting index
    * pos.
    */
   private static void putLong(byte[] array, long l, int pos,
      int numberOfLongBytes)
   {
      for(int i = 0; i < numberOfLongBytes; ++i) {
         array[pos + numberOfLongBytes - i - 1] = (byte) (l >>> (i * 8));
      }
   }

   @Override
   public String getBase64UUID() {
      final int sequenceId = sequenceNumber.incrementAndGet() & 0xffffff;
      long timestamp = System.currentTimeMillis();

      synchronized(this) {
         // Don't let timestamp go backwards, at least "on our watch" (while
         // this JVM is running). We are still vulnerable if we are
         // shut down, clock goes backwards, and we restart... for this we
         // randomize the sequenceNumber on init to decrease chance of
         // collision:
         timestamp = Math.max(lastTimestamp, timestamp);

         if(sequenceId == 0) {
            // Always force the clock to increment whenever sequence number is
            // 0, in case we have a long time-slip backwards:
            timestamp++;
         }

         lastTimestamp = timestamp;
      }

      final byte[] uuidBytes = new byte[15];

      // Only use lower 6 bytes of the timestamp (this will suffice beyond the
      // year 10000):
      putLong(uuidBytes, timestamp, 0, 6);

      // MAC address adds 6 bytes:
      System.arraycopy(secureMungedAddress, 0, uuidBytes, 6,
         secureMungedAddress.length);

      // Sequence number adds 3 bytes:
      putLong(uuidBytes, sequenceId, 12, 3);

      assert 9 + secureMungedAddress.length == uuidBytes.length;

      byte[] encoded;

      try {
         encoded =
            Base64.encodeBytesToBytes(uuidBytes, 0, uuidBytes.length,
               Base64.URL_SAFE);
      }
      catch(IOException e) {
         throw new IllegalStateException("should not be thrown", e);
      }

      // We are a multiple of 3 bytes so we should not see any padding:
      assert encoded[encoded.length - 1] != '=';
      return new String(encoded, 0, encoded.length, Base64.PREFERRED_ENCODING);
   }
}