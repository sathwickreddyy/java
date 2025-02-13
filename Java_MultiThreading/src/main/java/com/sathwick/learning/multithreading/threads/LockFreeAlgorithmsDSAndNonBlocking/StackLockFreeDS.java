package com.sathwick.learning.multithreading.threads.LockFreeAlgorithmsDSAndNonBlocking;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class StackLockFreeDS {

    public static void main(String[] args) {
//        StandardStack<Integer> stack = new StandardStack<>();
        LockFreeStack<Integer> stack = new LockFreeStack<>();

        Random random   = new Random();
        for(int i=0; i<10000; i++){
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();

        int pushingThreads = 2;
        int poppingThreads = 2;

        for(int i=0; i<pushingThreads; i++){
            Thread thread = new Thread(() -> {
                while(true){
                    stack.push(random.nextInt());
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
        }

        for(int i=0; i<poppingThreads; i++){
            Thread thread = new Thread(() -> {
                while(true){
                    stack.pop();
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
        }

        threads.forEach(Thread::start);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("No of operations performed in 10s are Counter: " + stack.getCounter());
        // StandardStack On average 251million operations: No of operations performed in 10s are Counter: 250998271
        // LockFreeStack On average done 845 million operations: No of operations performed in 10s are Counter: 844773078

    }

    private static class LockFreeStack<T> {
        private AtomicReference<StackNode<T>> head = new AtomicReference<>();

        private AtomicInteger counter = new AtomicInteger(0);

        public void push(T element) {
            StackNode<T> newHead = new StackNode<>(element);

            while(true){ // many threads can push or pop, so we may need few attempts to succeed
                StackNode<T> currentHead = head.get();
                newHead.next = currentHead;
                if(head.compareAndSet(currentHead, newHead)){
                    break;
                }
                else {
                    LockSupport.parkNanos(1);
                }
            }
            counter.incrementAndGet();
        }

        public T pop() {
            StackNode<T> currentHead = head.get();
            StackNode<T> newHead;

            while(currentHead != null){
                newHead = currentHead.next;
                if(head.compareAndSet(currentHead, newHead)){
                    break;
                }
                else { // the head has changed by other thread after we read from head variable.
                    LockSupport.parkNanos(1);
                    currentHead = head.get();
                }
            }
            counter.incrementAndGet();
            return currentHead != null ? currentHead.data : null;
        }

        public int getCounter() {
            return counter.get();
        }
    }

    private static class StandardStack<T>{
        private StackNode<T> head;
        @Getter
        private int counter;

        public synchronized void push(T element){
            StackNode<T> newHead = new StackNode<>(element);
            newHead.next = head;
            counter++;
        }

        public synchronized T pop(){
            if (head == null) {
                counter++;
                return null;
            }

            T value = head.data;
            head = head.next;
            counter++;
            return value;
        }

    }

    private static class StackNode<T> {
        T data;
        StackNode<T> next;
        public StackNode(T data) {
            this.data = data;
            this.next = null;
        }
    }
}
