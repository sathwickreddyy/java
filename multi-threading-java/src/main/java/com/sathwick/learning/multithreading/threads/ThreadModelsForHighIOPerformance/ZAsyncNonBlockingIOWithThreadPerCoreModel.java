package com.sathwick.learning.multithreading.threads.ThreadModelsForHighIOPerformance;


/*
    Why?

    Thread per task threading model doesn't always give optimal performance
        - When a thread is blocking on IO, it cannot be used for other purposes.
        - Requires us to allocate more threads.
        - Consuming more resources.
        - Adding Context-Switch Overhead.

    Non-BlockingIO - Introduction
        public void someMethod(){
            // some logic
            nonBlockingIO(); // Doesn't block CPU Operation.
            // some logic
        }

    Or

    public void handleRequest(HttpExchange exchange){
        Request request = parseUserRequest(exchange);
        readFromDBAsync(request, data -> {
           sendPageToUser(data, exchange);
        });
    }

    Req 1 -----> Thread 1 performs parseRequest, Instead of blocking when there is a readFromDB the thread become
    available for other requests. When readFromDB completes, it notifies a waiting thread and resumes execution.
    Here a single thread can handle multiple requests. instead of being blocked for a long time. Here there are
    very few context switches or no context switches in most of the time.

    If more cores then one thread per core will handle effectively to handle the requests.

    Benefits of Thread per Core Model + Non Blocking IO provides:
        - Very efficient for I/O bound tasks. (Optimal performance)
        - Minimal resource consumption.
        - No context switches or very few context switches.
        - Better utilization of CPU resources.
        - Better performance compared to thread per-request model.
        - ** Provides stability and security against issues or crashes of other systems.

 */

public class ZAsyncNonBlockingIOWithThreadPerCoreModel {

}
