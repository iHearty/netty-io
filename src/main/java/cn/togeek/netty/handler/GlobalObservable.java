package cn.togeek.netty.handler;

import java.util.Observable;

public class GlobalObservable extends Observable {
   public static GlobalObservable INSTANCE = new GlobalObservable();

   @Override
   public void notifyObservers(Object arg) {
      setChanged();
      super.notifyObservers(arg);
   }

   @Override
   public void notifyObservers() {
      setChanged();
      super.notifyObservers();
   }

   private GlobalObservable() {
   }
}