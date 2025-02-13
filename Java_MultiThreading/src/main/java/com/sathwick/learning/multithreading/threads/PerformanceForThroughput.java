package com.sathwick.learning.multithreading.threads;

import java.io.OutputStream;
import java.util.concurrent.Executors;
/*

    Throughput: How many tasks can be completed in a given time.
    Throughput = Number of Jobs / Time

    When does this matter?


    Task 1 ---->
    Task 2 ---->
                        System
    Task 3 ---->
    Task 4 ---->

    2 approaches to improve throughput of an application.

    Approach 1: Breaking Tasks into SubTasks
        OriginalTask -> Takes Latency T then throughput = 1/T

            Thread 1 -> SubTask 1 -> 10ms
            Thread 2 -> SubTask 2 -> 5ms
            Thread 3 -> SubTask 3 -> 50ms
            Thread 4 -> SubTask 4 -> 100ms
            Total Time = 10 + 5 + 50 + 100 = 165ms

            Latency = T/N where T is the total time taken to execute the original task
            and N is the number of tasks.
            Throughput = N/T = 1/165 = 0.006

            Cost of parllelzation and aggreation is breaking into subtasks + thread scheduling and aggregating later

    Approach 2: Parallel Processing (Running Tasks in parallel)

            Task1 -> 10ms
            Task2 -> 5ms
            Task3 -> 50ms
            Task4 -> 100ms

            Total Time = 165ms

            Throughput = N/T = 4/165 = 0.023

            here we dont require

             Breaking task into multiple tasks (Not required)

             Thread creation, passing tasks to threads
             +
             Time between thread.start() to thread getting scheduled

             Time until the last thread finishes and signals (Not required)

             Time until the aggregating thread runs (Not required)

             Aggregating of the sub results into a single artifact (Not required)

             We can minimize the Thread creation etc. through some other techniques like
             Thread Pool, Executor Framework etc.
 */


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    Thread Pooling: Creating the threads once and reusing them for multiple tasks.
                    Once created they sit in pool and can be reused.

                    Note: if all the threads are busy then the task has to wait.
                    If we keep the thread pool engaged and busy then we can achieve maximum through put we need.
                    Not trivial to implement a thread pool.

    Executor Framework: Similar to Thread Pooling but with more features like
                        - Graceful shutdown
                        - Metrics tracking
                        - Dynamic pool adjustment
                        - Blocking queue for tasks

    Fixed thread pool executor
    int numberOfThreads = 4;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    Runnable task = ....;

    executor.execute(task);
 */
public class PerformanceForThroughput {
    /*
         HTTP Server and measure the throughput using Apache JMeter

         HTTP Req ->
         HTTP Req ->
         HTTP Req ->
         HTTP Req ->
                                HTTP Server: Holds a WAR and piece book (lengthy one)
         HTTP Req ->
         HTTP Req ->
         HTTP Req ->
         HTTP Req ->


         Say HTTP Req: localhost:8000/search?word=talk
         Response should return no. of times the word appeared in the book.
         ex: HTTP Res: status: 200, body: 3309
     */

    private static final String INPUT_FILE = "src/main/java/com/sathwick/learning/multithreading/resources/throughput/war_and_peace.txt";
    private static final int NUMBER_OF_THREADS = 4;

    public static void main(String[] args) throws IOException {
        // print no of cores of this machine
        System.out.println("No of cores: " + Runtime.getRuntime().availableProcessors());


        String text = new String(Files.readAllBytes(Paths.get(INPUT_FILE)));
        startServer(text);
    }

    public static void startServer(String text) throws IOException {
        // Server code goes here
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/search", new WordCountHandler(text));
        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        server.setExecutor(executor);
        server.start();
    }

    private static class WordCountHandler implements HttpHandler {

        private final String text;

        public WordCountHandler(String text) {
            this.text = text;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            // Handler code goes here
            String query = httpExchange.getRequestURI().getQuery();
            String[] keyAndValue = query.split("=");
            String action = keyAndValue[0];
            String word = keyAndValue[1];
            if(!action.equals("word")) {
                httpExchange.sendResponseHeaders(400, 0);
                return;
            }

            long count = countWord(word);

            byte[] response = Long.toString(count).getBytes();
            httpExchange.sendResponseHeaders(200, response.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response);
            outputStream.close();
        }

        private long countWord(String word) {
            long count = 0;
            for(String w: text.split(" ")) {
                if(w.equals(word)) {
                    count++;
                }
            }
            return count;
        }
    }

}
