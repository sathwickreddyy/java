package com.sathwick.learning.multithreading.threads.concurrency;


/*
    Atomic Operations

    Which operations are atomic? (Mostly most of them are not atomic)
    -> All reference assignments are atomic
        Object a = new Object();
        Object b = new Object();
        a = b; // atomic
        i.e., all getters and setters are atomic, and we don't need to add synchronize on it.

     -> All assignments to primitives except long and double are atomic.
        > long and double are not atomic because they are 64 bit and 64 bit can be divided into 32 bit

     To make it atomic we can use a keyword called volatile.


     Metrics Aggregation.

     When we run applications on prod environments, we have to analyze how much time it
     is taking to process a request or to complete a task. To measure this, we have
     metrics aggregation.


     Summary:
         Atomic: Read and write operations on variables are atomic.
         Volatile: Volatile variables are always visible to all threads.
         Synchronize: Synchronization is used for thread safety.
         Atomicity: Atomic operations are used for performance.
         Metrics: Metrics aggregation is used for performance analysis.

         Atomic Operations are:
            > Assignments to primitive data types excepts for double and long
            > Assignments to references
            > Assignments to double and long with volatile keyword

 */

import java.util.Random;

public class AtomicVolatileMetrics {

    public static void main(String[] args) {
        Metrics metrics = new Metrics();
        BusinessLogic businessLogic1 = new BusinessLogic(metrics);
        BusinessLogic businessLogic2 = new BusinessLogic(metrics);
        MetricsPrinter metricsPrinter = new MetricsPrinter(metrics);

        businessLogic1.start();
        businessLogic2.start();
        metricsPrinter.start();
    }

    public static class MetricsPrinter extends Thread {
        private final Metrics metrics;

        public MetricsPrinter(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void run() {
            while(true){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                double currentAverage = metrics.getAverage();
                System.out.println("Current Average is " + currentAverage);
            }
            // x = int_max
        }
    }

    public static class BusinessLogic extends Thread {
        private final Metrics metrics;
        private final Random random = new Random();

        public BusinessLogic(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void run() {
            while(true){
                long start = System.currentTimeMillis();
                try {
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long end = System.currentTimeMillis();
                metrics.addSample(end - start);
            }

        }
    }


    public static class Metrics {
        private long count = 0; // 64 bit
        private volatile double average = 0.0;
        // this volatile keyword will guarantee write on addSample and read on getAverage is atomic
        // volatile simple means the data will be access from RAM.


        // potentially many threads can add samples here
        public synchronized void addSample(long sample) {
            double currentSum = this.count * this.average;
            this.count++;
            this.average = (currentSum + sample) / this.count;

        }

        public double getAverage() {
            return this.average;
        }
    }

}
