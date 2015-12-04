package cn.togeek.netty.rpc;

public enum TransportType {
   REQUEST(1), RESPONSE(2);

   private final int type;

   TransportType(int type) {
      this.type = type;
   }

   public int getType() {
      return type;
   }

   public static boolean isRequset(int type) {
      return REQUEST.type == type;
   }

   public static boolean isResponse(int type) {
      return RESPONSE.type == type;
   }
}