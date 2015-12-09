package cn.togeek.netty.concurrent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import cn.togeek.netty.Settings;
import cn.togeek.netty.exception.SettingsException;

public class ThreadPool {
   public static class Names {
      public static final String SAME = "same";

      public static final String GENERIC = "generic";

      public static final String SUGGEST = "suggest";

      public static final String REFRESH = "refresh";
   }

   public enum ThreadPoolType {
      CACHED("cached"),
      DIRECT("direct"),
      FIXED("fixed"),
      SCALING("scaling");

      private final String type;

      public String getType() {
         return type;
      }

      ThreadPoolType(String type) {
         this.type = type;
      }

      private final static Map<String, ThreadPoolType> TYPE_MAP;

      static {
         Map<String, ThreadPoolType> typeMap = new HashMap<>();

         for(ThreadPoolType threadPoolType : ThreadPoolType.values()) {
            typeMap.put(threadPoolType.getType(), threadPoolType);
         }

         TYPE_MAP = Collections.unmodifiableMap(typeMap);
      }

      public static ThreadPoolType fromType(String type) {
         ThreadPoolType threadPoolType = TYPE_MAP.get(type);

         if(threadPoolType == null) {
            throw new IllegalArgumentException("no ThreadPoolType for " + type);
         }

         return threadPoolType;
      }
   }

   public static Map<String, ThreadPoolType> THREAD_POOL_TYPES;

   static {
      HashMap<String, ThreadPoolType> map = new HashMap<>();
      map.put(Names.SAME, ThreadPoolType.DIRECT);
      map.put(Names.GENERIC, ThreadPoolType.CACHED);
      map.put(Names.SUGGEST, ThreadPoolType.FIXED);
      map.put(Names.REFRESH, ThreadPoolType.SCALING);
      THREAD_POOL_TYPES = Collections.unmodifiableMap(map);
   }

   private static void add(Map<String, Settings> executorSettings,
                           ExecutorSettingsBuilder builder)
   {
      Settings settings = builder.build();
      String name = settings.get("name");
      executorSettings.put(name, settings);
   }

   private static class ExecutorSettingsBuilder {
      Map<String, String> settings = new HashMap<>();

      public ExecutorSettingsBuilder(String name) {
         settings.put("name", name);
         settings.put("type", THREAD_POOL_TYPES.get(name).getType());
      }

      public ExecutorSettingsBuilder size(int availableProcessors) {
         return add("size", Integer.toString(availableProcessors));
      }

      public ExecutorSettingsBuilder queueSize(int queueSize) {
         return add("queue_size", Integer.toString(queueSize));
      }

      public ExecutorSettingsBuilder keepAlive(int keepAlive) {
         return add("keep_alive", Integer.toString(keepAlive));
      }

      private ExecutorSettingsBuilder add(String key, String value) {
         settings.put(key, value);
         return this;
      }

      public Settings build() {
         return Settings.builder().put(settings).build();
      }
   }

   public static ThreadPool INSTANCE = new ThreadPool();

   private static final Executor DIRECT_EXECUTOR = new Executor() {
      @Override
      public void execute(Runnable command) {
         command.run();
      }
   };

   private static final Logger logger = Logger
      .getLogger(ThreadPool.class.getName());

   private volatile Map<String, ExecutorHolder> executors;

   private final ScheduledThreadPoolExecutor scheduler;

   private ThreadPool() {
      int availableProcessors = Executors.boundedNumberOfProcessors();
      int halfProcMaxAt10 = Math.min(((availableProcessors + 1) / 2), 10);
      Map<String, Settings> defaultExecutorTypeSettings = new HashMap<>();
      add(defaultExecutorTypeSettings,
         new ExecutorSettingsBuilder(Names.GENERIC).keepAlive(30 * 1000));
      add(defaultExecutorTypeSettings,
         new ExecutorSettingsBuilder(Names.REFRESH).size(halfProcMaxAt10)
            .keepAlive(5 * 60 * 1000));
      add(defaultExecutorTypeSettings,
         new ExecutorSettingsBuilder(Names.SUGGEST).size(availableProcessors)
            .queueSize(1000));

      Map<String, ExecutorHolder> executors = new HashMap<>();

      for(Map.Entry<String, Settings> executor : defaultExecutorTypeSettings
         .entrySet())
      {
         executors.put(executor.getKey(),
            build(executor.getKey(), executor.getValue()));
      }

      executors.put(Names.SAME, new ExecutorHolder(DIRECT_EXECUTOR,
         new Info(Names.SAME, ThreadPoolType.DIRECT)));

      this.executors = Collections.unmodifiableMap(executors);
      this.scheduler = new ScheduledThreadPoolExecutor(1,
         Executors.daemonThreadFactory("scheduler"),
         new AbortPolicy());
      this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
      this.scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
      this.scheduler.setRemoveOnCancelPolicy(true);
   }

   public Info info(String name) {
      ExecutorHolder holder = executors.get(name);

      if(holder == null) {
         return null;
      }

      return holder.info;
   }

   public Executor generic() {
      return executor(Names.GENERIC);
   }

   public Executor executor(String name) {
      Executor executor = executors.get(name).executor();

      if(executor == null) {
         throw new IllegalArgumentException(
            "No executor found for [" + name + "]");
      }

      return executor;
   }

   public ScheduledExecutorService scheduler() {
      return this.scheduler;
   }

   public ScheduledFuture<?> schedule(int delay,
                                      String name,
                                      Runnable command)
   {
      if(!Names.SAME.equals(name)) {
         command = new ThreadedRunnable(command, executor(name));
      }

      return scheduler.schedule(command, delay, TimeUnit.MILLISECONDS);
   }

   public void shutdown() {
      scheduler.shutdown();

      for(ExecutorHolder executor : executors.values()) {
         if(executor.executor() instanceof ThreadPoolExecutor) {
            ((ThreadPoolExecutor) executor.executor()).shutdown();
         }
      }
   }

   public void shutdownNow() {
      scheduler.shutdownNow();

      for(ExecutorHolder executor : executors.values()) {
         if(executor.executor() instanceof ThreadPoolExecutor) {
            ((ThreadPoolExecutor) executor.executor()).shutdownNow();
         }
      }
   }

   public boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException
   {
      boolean result = scheduler.awaitTermination(timeout, unit);

      for(ExecutorHolder executor : executors.values()) {
         if(executor.executor() instanceof ThreadPoolExecutor) {
            result &= ((ThreadPoolExecutor) executor.executor())
               .awaitTermination(timeout, unit);
         }
      }

      return result;
   }

   private ExecutorHolder build(String name, Settings defaultSettings) {
      String type = defaultSettings.get("type", Names.SAME);
      ThreadPoolType threadPoolType = ThreadPoolType.fromType(type);
      ThreadFactory threadFactory = Executors.daemonThreadFactory(name);

      try {
         if(ThreadPoolType.DIRECT == threadPoolType) {
            return new ExecutorHolder(DIRECT_EXECUTOR,
               new Info(name, threadPoolType));
         }
         else if(ThreadPoolType.CACHED == threadPoolType) {
            if(!Names.GENERIC.equals(name)) {
               throw new IllegalArgumentException(
                  "thread pool type cached is reserved only for the generic "
                     + "thread pool and can not be applied to [" + name + "]");
            }

            int keepAlive =
               defaultSettings.getAsInt("keep_alive", 5 * 60 * 1000);
            Executor executor = Executors.newCached(name, keepAlive,
               TimeUnit.MILLISECONDS, threadFactory);
            return new ExecutorHolder(executor,
               new Info(name, threadPoolType, -1, -1, keepAlive, null));
         }
         else if(ThreadPoolType.FIXED == threadPoolType) {
            int size = defaultSettings.getAsInt("size",
               Executors.boundedNumberOfProcessors());
            int queueSize = defaultSettings.getAsInt("queue_size", 1000);
            Executor executor =
               Executors.newFixed(name, size, queueSize, threadFactory);
            return new ExecutorHolder(executor,
               new Info(name, threadPoolType, size, size, null, queueSize));
         }
         else if(ThreadPoolType.SCALING == threadPoolType) {
            int min = defaultSettings.getAsInt("min", 1);
            int size = defaultSettings.getAsInt("size",
               Executors.boundedNumberOfProcessors());
            int keepAlive =
               defaultSettings.getAsInt("keep_alive", 5 * 60 * 1000);
            Executor executor = Executors.newScaling(name, min, size,
               keepAlive, TimeUnit.MILLISECONDS, threadFactory);
            return new ExecutorHolder(executor,
               new Info(name, threadPoolType, min, size, keepAlive, null));
         }
      }
      catch(SettingsException e) {
         logger.log(Level.SEVERE, "settings error", e);
      }

      throw new IllegalArgumentException(
         "No type found [" + type + "], for [" + name + "]");
   }

   static class ExecutorHolder {
      private final Executor executor;

      public final Info info;

      ExecutorHolder(Executor executor, Info info) {
         this.executor = executor;
         this.info = info;
      }

      Executor executor() {
         return executor;
      }
   }

   public static class Info {

      private String name;

      private ThreadPoolType type;

      private int min;

      private int max;

      private Integer keepAlive;

      private Integer queueSize;

      Info() {
      }

      public Info(String name, ThreadPoolType type) {
         this(name, type, -1);
      }

      public Info(String name, ThreadPoolType type, int size) {
         this(name, type, size, size, null, null);
      }

      public Info(String name,
                  ThreadPoolType type,
                  int min,
                  int max,
                  Integer keepAlive,
                  Integer queueSize)
      {
         this.name = name;
         this.type = type;
         this.min = min;
         this.max = max;
         this.keepAlive = keepAlive;
         this.queueSize = queueSize;
      }

      public String getName() {
         return this.name;
      }

      public ThreadPoolType getThreadPoolType() {
         return this.type;
      }

      public int getMin() {
         return this.min;
      }

      public int getMax() {
         return this.max;
      }

      public Integer getKeepAlive() {
         return this.keepAlive;
      }

      public Integer getQueueSize() {
         return this.queueSize;
      }
   }

   class ThreadedRunnable implements Runnable {
      private final Runnable runnable;

      private final Executor executor;

      ThreadedRunnable(Runnable runnable, Executor executor) {
         this.runnable = runnable;
         this.executor = executor;
      }

      @Override
      public void run() {
         executor.execute(runnable);
      }

      @Override
      public int hashCode() {
         return runnable.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         return runnable.equals(obj);
      }

      @Override
      public String toString() {
         return "[threaded] " + runnable.toString();
      }
   }
}