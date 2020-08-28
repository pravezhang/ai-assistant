package com.lowrisk.aiassistant.global;

import java.util.concurrent.*;

public class Exe {
    private static final ExecutorService executor;

    static {
        executor = Executors.newCachedThreadPool();
    }
    public static boolean execute(Runnable runnable, long maxWaitInMills){
        Future<?> submit = executor.submit(runnable);
        try {
            Object o = submit.get(maxWaitInMills,TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            return false;
        }

    }
    public static <T> T executeRet(Callable<T> callable, long maxWaitInMills){
        Future<T> submit = executor.submit(callable);
        try {
            return submit.get(maxWaitInMills, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }
}
