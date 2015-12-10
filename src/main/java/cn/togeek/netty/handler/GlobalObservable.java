package cn.togeek.netty.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class GlobalObservable {
   public static class Types {
      public static final String REGISTRY = "REGISTRY";

      public static final String CONNECTED = "CONNECTED";
   }

   public static GlobalObservable INSTANCE = new GlobalObservable();

   public Map<String, Observable> map = new HashMap<>();

   private GlobalObservable() {
   }

   public synchronized void addObserver(String name, Observer observer) {
      Observable observable = map.get(name);

      if(observable == null) {
         observable = new InnerObservable();
         map.put(name, observable);
      }

      observable.addObserver(observer);
   }

   public synchronized void deleteObserver(String name, Observer observer) {
      Observable observable = map.get(name);

      if(observable != null) {
         observable.deleteObserver(observer);
      }
   }

   public synchronized int countObservers(String name) {
      Observable observable = map.get(name);

      if(observable != null) {
         return observable.countObservers();
      }

      return 0;
   }

   public synchronized boolean hasChanged(String name) {
      Observable observable = map.get(name);

      if(observable != null) {
         return observable.hasChanged();
      }

      return false;
   }

   public void notifyObservers(String name) {
      notifyObservers(name, null);
   }

   public void notifyObservers(String name, Object arg) {
      Observable observable = map.get(name);

      if(observable != null) {
         observable.notifyObservers(arg);
      }
   }

   private class InnerObservable extends Observable {
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
   }
}