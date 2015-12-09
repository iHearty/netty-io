package cn.togeek.netty.concurrent;

import cn.togeek.netty.exception.SettingsException;

public class ThreadPoolTest {
   public static void main(final String[] args) throws SettingsException {
      for(int i = 0; i < 100; i++)
         ThreadPool.INSTANCE.executor(ThreadPool.Names.SAME)
            .execute(new Runnable() {
               @Override
               public void run() {
                  try {
                     System.out.println(" here ---11 ");
                     Thread.sleep(1000);
                     System.out.println(" here ---22 ");
                  }
                  catch(InterruptedException e) {
                  }
               }
            });
      while(true) {
      }
   }
}