package cn.togeek.netty.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacAddressProvider {
   private static final Logger logger =
      Logger.getLogger(MacAddressProvider.class.getName());

   private static byte[] getMacAddress() throws SocketException {
      Enumeration<NetworkInterface> en =
         NetworkInterface.getNetworkInterfaces();

      if(en != null) {
         while(en.hasMoreElements()) {
            NetworkInterface nint = en.nextElement();

            if(!nint.isLoopback()) {
               // Pick the first valid non loopback address we find
               byte[] address = nint.getHardwareAddress();

               if(isValidAddress(address)) {
                  return address;
               }
            }
         }
      }

      // Could not find a mac address
      return null;
   }

   private static boolean isValidAddress(byte[] address) {
      if(address == null || address.length != 6) {
         return false;
      }

      for(byte b : address) {
         if(b != 0x00) {
            // If any of the bytes are non zero assume a good address
            return true;
         }
      }

      return false;
   }

   public static byte[] getSecureMungedAddress() {
      byte[] address = null;

      try {
         address = getMacAddress();
      }
      catch(SocketException se) {
         logger.log(Level.WARNING,
            "Unable to get mac address, will use a dummy address", se);
         // address will be set below
      }

      if(!isValidAddress(address)) {
         logger.log(Level.WARNING,
            "Unable to get a valid mac address, will use a dummy address");
         address = constructDummyMulticastAddress();
      }

      byte[] mungedBytes = new byte[6];
      SecureRandomHolder.INSTANCE.nextBytes(mungedBytes);

      for(int i = 0; i < 6; ++i) {
         mungedBytes[i] ^= address[i];
      }

      return mungedBytes;
   }

   private static byte[] constructDummyMulticastAddress() {
      byte[] dummy = new byte[6];
      SecureRandomHolder.INSTANCE.nextBytes(dummy);
      // Set the broadcast bit to indicate this is not a _real_ mac address
      dummy[0] |= (byte) 0x01;
      return dummy;
   }
}