package com.sathwick.learning.multithreading.threads.locking;
    /*

    Reentrant Lock:
        - Works just like a synchronized block
        - Can be acquired repeatedly by the same thread
        - But it requires explicit locking and unlocking
        - Example: Logger class

        Comparison:

        With synchronized:=
            Object lockObject = new Object();
            Resource resource = new Resource();
            ....
            ....
            public void method(){
                synchronized (lockObject){ -> lock
                    // Critical section
                    ....
                    use(resource)
                    ...
                }   -> unlock
            }

        With ReentrantLock:=
            Lock lockObject = new ReentrantLock();
            Resource resource = new Resource();
            ....
            ....
            public void method(){
                lockObject.lock(); -> lock
                    // Critical section
                    ....
                    use(resource)
                    ...
                lockObject.unlock();
            }

        Disadvantage:
            1: With return statements
            2: With throw statements

            example:
            Lock lock = new ReentrantLock();
            ...
            ...
            public int use() {
                lock.lock();
                ....

                return value; ---> returns value and lock remains intact which can lead to deadlock.
                lock.unlock();
            }

            Solution: using finally section.
            public int use() {
                lock.lock();
                try {
                    ....
                    return value;
                } finally {
                    lock.unlock();
                }
            }
    */

import lombok.Getter;
import lombok.Setter;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
        Why we need them?
        - Query methods for testing
            - getQueuedThreads() - Returns a list of threads waiting to acquire a lock.
            - getOwner() - Returns the thread that currently owns a lock.
            - isHeldByCurrentThread() - Returns true if the current thread holds the lock.
            - isLocked() - Returns true if the lock is currently held by any thread.
            - tryLock() - Acquires the lock if possible, returning true if successful.

        * Reentrant also gives us a feature of FAIRNESS
        -> Fairness - gives everyone chance to acquire the lock instead of starvation.
            - ReentrantLock(true)
            - May reduce the throughput of the application

        * Another main feature of Reentrant is LOCK INTERRUPTION
            -> ReentranctLock.lockInterruptibly()
            -> if a thread is interrupted while waiting for a lock, it throws InterruptedException

                Case: No use of interruption with locking mechanism either sync or  ReentrantLock

                    class SomeThread extends Thread {
                        public void run() {
                            while(true){
                                lockObject.lock();
                                ....
                                if(Thread.currentThread().isInterrupted()){
                                    cleanUpAndExit();
                                    break;
                                }
                                ....
                                lockObject.unlock();
                            }
                        }
                    }

                    somethread.interrupt(); =====> Does not do anything ****

                Solution:
                    class SomeThread extends Thread {
                        public void run() {
                            while(true){
                                try {
                                    lockObject.lockInterruptibly();2
                                    ....
                                } catch (InterruptedException e) {
                                    cleanUpAndExit();
                                    break;
                                }
                                ....
                                lockObject.unlock();
                            }
                        }
                    }
                    somethread.interrupt(); --> Works perfectly.
                    Wakes up the thread and exits gracefully.

           * Very important feature here
            tryLock feature***
            Reentrant.tryLock() -> Acquires the lock if possible and returns immediately, with the
            return value indicating whether the lock was acquired or not.
            -> If the lock is acquired, it returns true;

                                    lock() vs tryLock()

                ....                                         ....
                lockObject.lock();                  if(lockObject.tryLock()){
                try{                                    try{
                    ....                                       ....
                    use(resource)                           use(resource)
                    ....                                       ....
                }                                       }
                finally{                                finally{
                    lockObject.unlock();                   lockObject.unlock();
                }                                       }
                                                    }
                                                    else {...}
     */
public class ReEntrantLock {


    @Getter
    @Setter
    private static class PricesContainer {
        private Lock lockObject = new ReentrantLock();
        private double bitcoinPrice;
        private double etherPrice;
        private double litecoinPrice;
        private double bitcoinCashPrice;
        private double ripplePrice;
    }

    private static class PriceUpdater extends Thread{
        private PricesContainer pricesContainer;
        private Random random = new Random();
        public PriceUpdater(PricesContainer pricesContainer) {
            this.pricesContainer = pricesContainer;
        }
        @Override
        public void run() {
            while(true){
                pricesContainer.getLockObject().lock();
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                try {
                    pricesContainer.setBitcoinPrice(random.nextInt(2000));
                    pricesContainer.setEtherPrice(random.nextInt(1500));
                    pricesContainer.setLitecoinPrice(random.nextInt(1000));
                    pricesContainer.setBitcoinCashPrice(random.nextInt(1500));
                    pricesContainer.setRipplePrice(random.nextInt(1500));
                }
                finally {
                    pricesContainer.getLockObject().unlock();
                }

                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
