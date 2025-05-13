package com.sathwick.learning.multithreading.threads.locking;

/*
    Deadlocks:
        > Two or more threads are waiting for each other to release locks.
        > This can happen in 2 ways:
            > Mutual Exclusion
            > Hold and Wait
            > Non-preemptive allocation - A resource is released only after the thread
            is done using it
            > Circular Wait - A set of threads are waiting for each other in a circular

      Thread 1:                     Thread 2:
        1. lock(A)
                                    2. lock(B)
                                    3. lock(A) // Not possible as Thread 1 needs to release
        4. lock(B) // Thread1 to progress it needs to acquire lock on B
                DEADLOCK

        Solution:
            - Avoid circular waits : Enforce a strict order of locking acquisition
            - Easy for smaller codes

        Thread 1 and thread 2 in above example is an example of circular wait. To avoid
        Thread 2 also acquires lock on A and then acquires lock on B. This will not result in
        a deadlock situation.


        Other technique: *****
            - Deadlock technique - Watchdog
            - Thread interruption (not possible with synchronized). **
            - tryLock() operations (not possible with synchronized). **
 */


import java.util.Random;

public class LockingAndDeadLocks {
    // Case Study: Railway Signals intersection problem

    public static void main(String[] args) {
        Intersection intersection = new Intersection();
        Thread trainA = new Thread(new TrainA(intersection));
        Thread trainB = new Thread(new TrainB(intersection));
        trainA.start();
        trainB.start();
        // At some point of the execution stops and waits for each other
        // this is called a deadlock situation.
    }

    public static class TrainA implements Runnable{
        private Intersection intersection;
        private Random random = new Random();

        public TrainA(Intersection intersection) {
            this.intersection = intersection;
        }

        @Override
        public void run() {
            try {
                while(true){
                    Thread.sleep(random.nextInt(5));
                    intersection.takeRoadA();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static class TrainB implements Runnable{
        private Intersection intersection;
        private Random random = new Random();

        public TrainB(Intersection intersection) {
            this.intersection = intersection;
        }

        @Override
        public void run() {
            try {
                while(true){
                    Thread.sleep(random.nextInt(5));
                    intersection.takeRoadB();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Intersection {
        private Object roadA = new Object();
        private Object roadB = new Object();

        public void takeRoadA() throws InterruptedException {
            synchronized (roadA) {
                System.out.println("Road A is locked by thread "+ Thread.currentThread().getName());
                synchronized (roadB) {
                    System.out.println("Train is passing through road A");
                    Thread.sleep(1);
                }
            }
        }

        public void takeRoadB() throws InterruptedException {
            synchronized (roadA) {
                System.out.println("Road B is locked by thread "+ Thread.currentThread().getName());
                synchronized (roadB) {
                    System.out.println("Train is passing through road B");
                    Thread.sleep(1);
                }
            }
        }
    }
}
