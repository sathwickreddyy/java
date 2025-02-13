package com.sathwick.learning.multithreading.threads.LockFreeAlgorithmsDSAndNonBlocking;


/*
   What's wrong with locks?

   - Everything we've learned so far about locks is very valuable
   - Majority of multi-threaded programming is still done with locks (synchronized, ReentrantLock,
   ReentrantReadWriteLock, etc)
   - Most of the concurrency problems are easier and safer to solve with locks.
   - Using locks we can solve all the concurrency problems.

    Why learning Lock Free techniques?
    - As engineers we are always faced with trade off.
    - For most problems there's more than one solution
    - The more tools with have the better we can choose the right tool for the job.
    - Being able to choose the right tool for the job is a very important skill in being a good engineer.

    Problems with Locks?
    - Deadlocks are generally unrecoverable.
    - Can bring the application to a complete halt if the locks are not acquired properly.
    - Can cause scalability issues as the locks can become a bottleneck.
    - Can lead to starvation if threads are not given enough CPU time.
    - Can lead to incorrect results due to race conditions.
    - Priority Inversion
        - Low priority thread (document saver)
        - High priority thread (user interface)
        - Low priority thread acquires a lock on a resource but is preempted by CPU (scheduled out)
        - High priority cannot progress because of the low priority thread holding the lock and not
        scheduled to release the lock.
    - Kill Tolerance
        - Thread dies, gets interrupted or forgets to release a lock.
        - Leaves all thread hanging forever.
        - Can lead to resource leaks.
    - Performance Issues
        - Performance overhead in having contention over a lock.
            - Thread A acquires a lock
            - Thread B tries to acquire a lock and gets locked.
            - Thread B is scheduled out (Context switch).
            - Thread B is scheduled back (context switch).
         - Additional overhead may not be noticeable for most applications but for latency
         sensitive applications, this overhead can be significant.
 */


/*
    Lock Free Solutions
        - Utilize operations which are guaranteed to be one hardware operation.
        - A single hardware operation is
            - Atomic by definition
            - Thread safe
            - Read/Assignment on all primitive types except double and long
            - Write on all reference types
            - Write on volatile and double and long types with volatile keyword
        - Read/Assignments on all volatile primitive types and references.

    Atomic Classes
        - java.util.concurrent.atomic package
        - Consist of classes which support lock free thread safe operations on primitive types and
        references.
        - Internally uses the Unsafe class which provides access to low level, native methods.
        - Utilize platform specific implementation of atomic operations.
        - AtomicX Classes
            - AtomicBoolean
            - AtomicInteger
            - AtomicLong
            - AtomicReference
            - AtomicMarkableReference
            - AtomicReferenceArray
            - AtomicIntegerFieldUpdater
            etc...

 */

public class AtomicInteger {
    /*
        int intialValue = 0
        AtomicInteger atomicInteger = new AtomicInteger(initialValue);

        // Automatically increment the integer by one.
        atomicInteger.incrementAndGet();    // return the new value
        atomicInteger.getAndIncrement();    // return the previous value

        // Or
        int delta = 5;
        atomicInteger.addAndGet(delta); // return the new value can sometimes lead to race conditions
        atomicInteger.getAndAdd(delta); // return the previous value

        // decrement
        atomicInteger.decrementAndGet();

        Pros:
            - Simplicity
            - Thread safe
            - No need to worry about race conditions.
        Cons:
            - Performance overhead.
            - Only the operation itself is atomic.
            - There's still race conditions between 2 separate atomic operations.

     */
}
