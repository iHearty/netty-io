package cn.togeek.netty.rpc;

public class TransportStatus {
   private static final int STATUS_REQRES = 0x01;

   private static final int STATUS_ERROR = 0x02;

   private static final int STATUS_HEARTBATE = 0x04;

   public static boolean isRequest(int value) {
      return (value & STATUS_REQRES) == 0;
   }

   public static int setRequest(int value) {
      value &= ~STATUS_REQRES;
      return value;
   }

   public static int setResponse(int value) {
      value |= STATUS_REQRES;
      return value;
   }

   public static boolean isError(int value) {
      return (value & STATUS_ERROR) != 0;
   }

   public static int setError(int value) {
      value |= STATUS_ERROR;
      return value;
   }

   public static boolean isHeartbate(int value) {
      return (value & STATUS_HEARTBATE) != 0;
   }

   public static int setHeartbate(int value) {
      value |= STATUS_HEARTBATE;
      return value;
   }
}