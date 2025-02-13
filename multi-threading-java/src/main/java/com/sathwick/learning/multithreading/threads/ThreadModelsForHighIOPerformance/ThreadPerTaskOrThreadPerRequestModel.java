package com.sathwick.learning.multithreading.threads.ThreadModelsForHighIOPerformance;

/*
 * This class demonstrates blocking I/O operations.
 * Blocking I/O operations can significantly affect the performance of a program,
 * especially when dealing with network operations or file I/O.
 * By using blocking I/O operations, a thread can only perform other tasks
 * while waiting for the I/O operation to complete.
 *
 *
 * When a task involves blocking calls, then using thread pools #threads = #cores
 *  - Doesn't give us the best performance
 *  - Doesn't give us the best CPU Utilization
 *
 * */

/*

    User1 --------------------|
    User2 --------------------|
    User3 --------------------|
    .                         |  N Requests
    .                         -------------->                 ---------->
    .                         -------------->  WebApplication ----------> Database (IO Operations)
    .                         -------------->                 ---------->
    .                         |
    .                         |
    .                         |
    UserN --------------------|

    Lets each blocking call takes 1 second
    Each task on separate thread with N threads in the pool should take one second to process all the requests and respond
    But if encase we use separate thread per core then with multiple IO calls and multiple context switches causes the performance go down.
    So, if we use ThreadPerTask or ThreadPerRequest then it will be better where there are blocking IO calls.
 */

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPerTaskOrThreadPerRequestModel {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int NUMBER_OF_TASKS = 10000;
    /*
        [6.922s][warning][os,thread] Failed to start the native thread for java.lang.Thread "pool-1-thread-4068"
        Exception in thread "main" java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached
            at java.base/java.lang.Thread.start0(Native Method)
            at java.base/java.lang.Thread.start(Thread.java:809)
            at java.base/java.util.concurrent.ThreadPoolExecutor.addWorker(ThreadPoolExecutor.java:945)
            at java.base/java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1364)
            at java.base/java.util.concurrent.AbstractExecutorService.submit(AbstractExecutorService.java:123)
            at com.sathwick.learning.multithreading.threads.ThreadModelsForHighIOPerformance.ThreadPerTaskModel.performTasks(ThreadPerTaskModel.java:62)
            at com.sathwick.learning.multithreading.threads.ThreadModelsForHighIOPerformance.ThreadPerTaskModel.main(ThreadPerTaskModel.java:51)
     */

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press enter to start");
        scanner.nextLine();
        System.out.printf("Running %d tasks \n", NUMBER_OF_TASKS);

        long start = System.currentTimeMillis();
        performTasks();
        long end = System.currentTimeMillis();
        System.out.println("Time taken - "+ (end - start)); // 68 ms

        start = System.currentTimeMillis();
        performTasksWithMoreIOCalls();
        end = System.currentTimeMillis();
        System.out.println("Time taken with more IO calls - "+ (end - start)); // 2257 ms
        // why more time is taken in second case, because each thread is calling 100 IO calls and there are around 99
        // context switches among themselves which is causing more overhead on the CPU.
    }

    private static void performTasks() {
        try {
//            ExecutorService executorService = Executors.newCachedThreadPool(); // Allocating to many threads can lead to crashing of application.
            // Dynamic thread pool will grow to as many as we need to complete those tasks and cache those threads to be reused in future
            // In real world we never know how many tasks we need to perform in a given moment and therefore we cannot determine to preallocate
            // fixed thread pool is a better option in that case
            ExecutorService executorService = Executors.newFixedThreadPool(1000); // through put: 1000 operations per second
            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(() -> blockingIOOperation(1000)); // Single blocking IO per thread with 1000ms waiting
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void performTasksWithMoreIOCalls(){
        try {
            ExecutorService executorService  = Executors.newFixedThreadPool(1000);
            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executorService.submit(() -> {
                    for(int j=0; j<100; j++){
                        blockingIOOperation(10); // almost same operation 100 blockingIO calls with 10ms waiting time
                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void blockingIOOperation(int sleepTimeMs){
//        System.out.println("Executing a blocking task from thread "+ Thread.currentThread().getName());
        try {
            Thread.sleep(sleepTimeMs); // Simulating a blocking I/O operation
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * Advantages of thread per task model over thread per core model is:
 *  - Improvement for:
 *      - Throughput
 *      - Hardware utilization
 *      - Resource utilization
 *
 *   - Processed tasks concurrently and completed them faster than in the thread-per-core model.
 *
 *   Cons/Issues:
 *   - Threads are expensive:
 *      - No. of threads we can create is limited.
 *      - Threads consume stack memory and other resources
 *      - Too many threads - Our application will crash
 *      - Too few threads - Idle threads consuming CPU cycles or Lower throughput and CPU utilization.
 *
 *
 */
