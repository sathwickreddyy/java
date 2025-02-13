package com.sathwick.learning.multithreading.threads.virtualthreads;

/*
    Introduced in JDK 19/21+

    Like platform threads, virtual threads contains run(), start() but OS doesn't take any responsibility
    in creating/managing and is not even aware of it. It is managed by the JVM.
    - Also virtual threads doesn't have fixed sized stack assigned to it, and
    it uses the Heap Space and also can be reclaimed by JVM Garbage collector when it is no longer needed.

    - Unlike platform threads, virtual threads doesn't have any dependencies on the underlying OS.
    - Virtual threads are designed to be lightweight and cheap to create and manage unlike platform thread which is costlier to create.

    How does virtual threads run in CPU Core if no OS is involved?

        > For this purpose, as soon as we create virtual thread, under the hood the jvm will create
        a relatively smaller internal pool of platform threads and runs it.
        > When JVM wants to run particular virtual thread, it will be MOUNTED to this platform thread called as Carrier thread
        > Once the virtual threads completes its task, it will be UNMOUNTED from the carrier thread and make that carrier
        thread available for other virtual threads to use.

    Advantages of Virtual Threads
        - Cost effective
        - No thread management overhead
        - No context-switch overhead

    Virtual threads are created using ThreadFactory,


 */


public class VirtualThreads {

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> System.out.println("Inside thread "+ Thread.currentThread());
        Thread virtualThread = Thread.ofPlatform().start(runnable);
        virtualThread.start();
        virtualThread.join();
    }

}

/*
    Best Practices

    > Use Virtual Threads when you want to leverage the benefits of lightweight threads without the overhead of
    thread management.
    > Use Virtual Threads when you want to perform blocking IO operations, as Virtual Threads doesn't consume CPU cycles
    while waiting for I/O operations to complete.

    Performance:
        1. Tasks involving only CPU but no IO or waiting then the virtual threads doesn't provide any benefit over platform threads
        2. Virtual threads doesn't provide any benefit inters of latency but provides better throughput because during waiting time from external devide (IO) we can
        reuse same virtual thread for other tasks.
        3. Short and frequent blocks calls are very inefficient.
        4. Virtual threads are better than using Platform threads directly in
            - Thread-per-task with platform threads introduce context-switch overhead.
            - Thread-per-request with Virtual threads have only JVM mounting/ unmounting overhead which is negligible compare to context-switch.

    Best practices:
        - Never create fixed-size pools of virtual threads because JVM takes care of creation and doesn't cause any out of memory exceptions.
            - Preferred way to use is using Executors.newVirtualThreadPerTaskExecutor() which internally uses a thread pool of virtual threads.
        - Virtual threads are always daemon threads. virtualThread.setDaemon(true) throws an exception.
        - Virtual threads always have default priority
            - virtualThread.setPriority(...) -> Doesn't do anything.
        - Note we may have a thousand/millions of virtual threads.




 */