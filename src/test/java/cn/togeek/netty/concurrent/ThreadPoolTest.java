package cn.togeek.netty.concurrent;

import cn.togeek.netty.Settings;
import cn.togeek.netty.exception.SettingsException;

public class ThreadPoolTest {
   public static void main(final String[] args) throws SettingsException {
      ThreadPool threadPool = new ThreadPool(Settings.EMPTY);

      for(int i = 0; i < 100; i++)
         threadPool.executor(ThreadPool.Names.SAME).execute(new Runnable() {
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