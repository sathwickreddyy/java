package com.sathwick.learning.multithreading.threads;

/*
 Why and when?
 -> Threads consume resources
    -> Memory and kernel resources
    -> CPU Cycles and cache memory

 -> If a thread finished its work, but the application is still running,
 we want to clean up the thread's resources.

 -> If a thread is misbehaving and is not terminating, we want to stop it.
 -> By default, the application will not stop as long as atleast one thread is still running.

 NOTE: If a thread is doing some computation and not catching/handling the interrupted signal then
 it will keep on running without exit.

 We have to explicitly handle that case.
 */

import java.math.BigInteger;
/*

    Each thread object have a method called interrupt

    Thread A                    Thread B
        |                          |
threadB.interrupt() -------|       |
        |                  |------->
        |                          x
                                   [InterruptedException]

 */
public class ThreadTermination {

    public static void main(String[] args) {
        Thread thread = new Thread(new BlockingTask());
        thread.start();
        // we want to interrupt the blocking thread.
        thread.interrupt();
    }


    private static class BlockingTask implements Runnable{
        @Override
        public void run() {
            try {
                System.out.println("BlockingTask Started ");
                Thread.sleep(500000);
            } catch (InterruptedException e) {
                System.out.println("Exiting blocking thread "+Thread.currentThread().getName());
            }
        }
    }
}


class Example2 {
    public static void main(String[] args) {
        Thread thread = new Thread(new LongComputationTask(new BigInteger("2"), new BigInteger("100000000000000000")));
        thread.start();

        thread.interrupt();
        // is not enough and we have no handler for that
        // the thread is still running.
        System.out.println("In Main");
    }


    private static class LongComputationTask implements Runnable{

        private BigInteger base, power;

        public LongComputationTask(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            System.out.println(base+"^"+power+" = "+pow(base, power));
        }

        private BigInteger pow(BigInteger base, BigInteger exponent){
            BigInteger result = BigInteger.ONE;
            for(BigInteger i=BigInteger.ZERO; i.compareTo(exponent) != 0; i=i.add(BigInteger.ONE)){
                // explicity write this logic or else the application keep on running even there is an interruption.
                if(Thread.currentThread().isInterrupted()){
                    System.out.println("Prematurely interrupted computation");
                    return BigInteger.ZERO;
                }
                result = result.multiply(base);
            }
            return result;
        }
    }

}