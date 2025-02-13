package com.sathwick.learning.multithreading.threads.concurrency;

/*
    Race Condition

    - Condition when multiple threads are accessing a shared resource concurrently.
    - Atleast one thread is modifying the resource.
    - The timing of threads' scheduling may cause incorrect results.
    - The core of the problem is non-atomic operations performed by multiple threads on a shared resource.
    - In simple words:
        Race Condition: When the output of a program depends on the order/thread of execution of independent


    Data Race - Example

    class SharedClass {
        int x=0;
        int y=0;

        public void increment(){ // called by thread 1 repeatedly
            x++;
            y++;
        }

        public void checkForDataRace() { // called by thread 2 repeatedly
            if (y > x) {
                throw new DataRaceException("y can't be greater than x");
            }
        }
    }

    if threads read as below
    case 1:

        checkForDataRace            increment
         t1: y <- 0
         t2: x <- 0
                                t2: x++;
                                t1: y++;
                   No issue: x==y

     case 2:
        checkForDataRace            increment
         t1: y <- 0
                                  t2: x++;
                                  t1: y++;
         t2: x <- 1
                    Issue: x>y

     case 3:

        checkForDataRace            increment
        t1: y <- 0
                                    t2: x++
        t2: x <- 1
                                    t1: y++
                                    ..
                                    t2: x++;
                                    t1: y++;
                    Issue x > y

     Some other cases holds y>x

     Why?

     - Compiler and CPU may execute the instructions out of order to optimise the
     performance and utilization.
 */

public class RaceAndDataRaceConditions {
    /*
        - Compiler and CPU may execute the instructions out of order to optimise the
        performance and utilization.
        - They will do so while maintaining the logical correctness of the code.
        - This is why we say, Compiler and CPU are "Transparently" Optimizing the code.
        - Out of order execution by the compiler and CPU are important features to speed
        up the code.

        The compiler re-arranges instructions for better
            - Branch prediction (optimised loops, if statements etc.)
            - Vectorization - parallel instruction example (SIMD)
            - Prefetching Instructions - better cache performance
        CPU re-arranges instructions for better hardware units utilization.

        Below is an example of CPU not rearranging instructions:
            public void someFunction() {
                x = 1;
                y = x+2;
                z = y+3;
            }

        Below is an example of CPU rearranging instructions:

        public void increment1(){
            x++;
            y++;
        }

        public void increment2(){
            y++;
            x++;
        }

        In the above case the order doesn't matter. Both are logically equivalent.
        We don't get any issue with single threaded application but this happens when
        there is a multithreading application.

        How to avoid DataRace?
            1. Synchronizations of methods which modify shared variables
            2. Declaration of shared variables as volatile keywords.

        Volatile Solution: ** in case of multi threaded application.
            volatile int sharedVar;

            public void method(){
                ...// All instructions will be executed before
                read/write(sharedVar)
                ...// All instructions will be executed after
            }

        Note: Synchronized - Solves both race condition and data race but performance as penalty.
              Volatile:
                - Solves Race Condition for read/write from/to long and double
                - Solves all Data Races by guaranteeing order
     */
    public static void main(String[] args) {
        SharedClass sharedClass = new SharedClass();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                sharedClass.increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                sharedClass.checkForDataRace();
            }
        });
        t1.start();
        t2.start();

        SharedClassWithVolatile  sharedClassWithVolatile = new SharedClassWithVolatile();

        Thread t3 = new Thread(() -> {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                sharedClassWithVolatile.increment();
            }
        });

        Thread t4 = new Thread(() -> {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                sharedClassWithVolatile.checkForDataRace();
            }
        });

        t3.start();
        t4.start();

    }

    public static class SharedClassWithVolatile{

        private volatile int x = 0;
        private volatile int y = 0;

        public void increment() {
            x++;
            y++;
        }

        public void checkForDataRace() {
            if (y > x || x < y) {
                throw new RuntimeException("y can't be greater than x");
            }
        }
    }

    public static class SharedClass {
        private int x = 0;
        private int y = 0;

        public int dataraceCount = 0;

        public void increment() {
            x++;
            y++;
        }

        public void checkForDataRace() {
            if (y > x) {
                ++this.dataraceCount;
//                throw new RuntimeException("y can't be greater than x");
                System.out.println("Shared class 1");
                System.out.println("y>x - Data Race is detected "+dataraceCount);
            }
        }
    }

}
