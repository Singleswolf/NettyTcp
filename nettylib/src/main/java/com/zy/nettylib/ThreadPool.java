package com.zy.nettylib;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
            10, 5000,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());


    public static void executeTask(Runnable runnable) {
        executor.execute(runnable);
    }

    public static void destroy(){
        executor.shutdownNow();
    }
}