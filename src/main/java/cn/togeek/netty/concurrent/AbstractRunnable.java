package cn.togeek.netty.concurrent;

/**
 * An extension to runnable.
 */
public abstract class AbstractRunnable implements Runnable {
   @Override
   public final void run() {
      try {
         doRun();
      }
      catch(InterruptedException ex) {
         Thread.interrupted();
         onFailure(ex);
      }
      catch(Throwable t) {
         onFailure(t);
      }
      finally {
         onAfter();
      }
   }

   /**
    * This method is called in a finally block after successful execution
    * or on a rejection.
    */
   public void onAfter() {
      // nothing by default
   }

   /**
    * This method is invoked for all exception thrown by {@link #doRun()}
    */
   public abstract void onFailure(Throwable t);

   /**
    * This should be executed if the thread-pool executing this action rejected
    * the execution.
    * The default implementation forwards to {@link #onFailure(Throwable)}
    */
   public void onRejection(Throwable t) {
      onFailure(t);
   }

   /**
    * This method has the same semantics as {@link Runnable#run()}
    * 
    * @throws InterruptedException if the run method throws an
    *            InterruptedException
    */
   protected abstract void doRun() throws Exception;
}