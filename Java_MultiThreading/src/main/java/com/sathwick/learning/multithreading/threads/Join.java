package com.sathwick.learning.multithreading.threads;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Join {

    public static void main(String[] args) throws InterruptedException {
        List<Long> inputNumbers = Arrays.asList(0L, 340000035L, 335L, 2342L, 24L, 23L, 3234L, 23000044L);
        // we want to calculate the factorial of each number in the list
        List<FactorialThread> threads = new ArrayList<>();

        for(long inputNumber: inputNumbers){
            threads.add(new FactorialThread(inputNumber));
        }

        for(Thread thread: threads){
            thread.setDaemon(true);
            thread.start();
            // now all the threads will start running concurrently
        }
        for(Thread thread: threads){

            thread.join(2000);
        }

        for(int i=0; i<inputNumbers.size(); i++){
            if(threads.get(i).isFinished()){
                System.out.println("Factorial of "+inputNumbers.get(i)+" is "+threads.get(i).getResult());
            } else {
                System.out.println("The calculation for "+inputNumbers.get(i)+" is still in progress");
            }
        }

        // now we want to join all the threads

    }

    private static class FactorialThread extends Thread {
        private final long inputNumber;
        private BigInteger result = BigInteger.ZERO;
        private boolean isFinished = false;

        public FactorialThread(long inputNumber) {
            this.inputNumber = inputNumber;
        }

        @Override
        public void run() {
            result = factorial(inputNumber);
            isFinished = true;
        }

        public BigInteger factorial(long n) {
            BigInteger tempResult = BigInteger.ONE;
            for (long i = n; i > 0; i--) {
                tempResult = tempResult.multiply(new BigInteger(Long.toString(i)));
            }
            return tempResult;
        }

        public boolean isFinished(){
            return isFinished;
        }
        public BigInteger getResult() {
            return result;
        }
    }

}
