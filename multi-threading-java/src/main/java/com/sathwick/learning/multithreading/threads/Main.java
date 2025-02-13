package com.sathwick.learning.multithreading.threads;

import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            System.out.println("We are in thread: " + Thread.currentThread().getName());
            System.out.println("Current thread priority is: " + Thread.currentThread().getPriority());
            throw new RuntimeException("oops");

        });

        thread.setName("New Worker thread");
        thread.setPriority(Thread.MAX_PRIORITY);
        System.out.println("We are in thread : "+Thread.currentThread().getName()+" before we started a new thread");
        thread.start(); // instruct jvm to create thread
        System.out.println("We are in thread : "+Thread.currentThread().getName()+" after we started a new thread");
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("A critical error happened in thread "+t.getName()+" the error is "+e.getMessage());
            }
        });
    }
}

/*

Case Study on threads : interactive mutli threading application

Design a secure volt, trying to secure my money and how long it would take to hackers to get into
the volt

We have 3 hacker threads trying to hack into the volt
We have 1 police thread who is going to arrest them.

 */

class CaseStudySecureVolt{
    public static final int MAX_PASSWORD = 9999;

    public static void main(String[] args) {
        Random random = new Random();

        Vault vault = new Vault(random.nextInt(MAX_PASSWORD));

        List<Thread> threads = new ArrayList<>();

        threads.add(new AscendingHackerThread(vault));
        threads.add(new DescendingHackerThread(vault));
        threads.add(new PoliceThread());

        threads.forEach(Thread::start);
    }

    private static class Vault{
        private int password;

        public Vault(int password){
            this.password = password;
        }

        public boolean isCorrectPassword(int guess){
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return this.password == guess;
        }

    }

    private static abstract class HackerThread extends Thread{
        protected Vault vault;
        public HackerThread(Vault vault){
            this.vault = vault;
            this.setName(this.getClass().getSimpleName());
            this.setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void start(){
            System.out.println("Starting thread "+this.getName());
            super.start();
        }
    }

    private static class AscendingHackerThread extends HackerThread{
        public AscendingHackerThread(Vault vault) {
            super(vault);
        }

        @Override
        public void run() {
            for(int guess=0; guess<MAX_PASSWORD; guess++){
                if(vault.isCorrectPassword(guess)){
                    System.out.println(this.getName()+" guessed the password "+guess);
                    System.exit(0);
                }
            }
        }

    }

    private static class DescendingHackerThread extends HackerThread{
        public DescendingHackerThread(Vault vault) {
            super(vault);
        }

        @Override
        public void run() {
            for(int guess=MAX_PASSWORD; guess>=0; guess--){
                if(vault.isCorrectPassword(guess)){
                    System.out.println(this.getName()+" guessed the password "+guess);
                    System.exit(0);
                }
            }
        }

    }

    private static class PoliceThread extends Thread{
        @Override
        public void run() {
            for(int i=10; i>0; i--){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(i);
            }
            System.out.println("Game over, you got caught!");
            System.exit(0);
        }
    }

}

