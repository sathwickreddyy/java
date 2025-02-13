package com.sathwick.learning.multithreading.threads.interthreadcommunication;

/*
    - Can be used to restrict the number of users to a particular resource or a group of resources.
    - Unlike the locks that allows only one user at a time per resource.
    - The semaphore can restrict any given number of users to a resource or a set of resources.


    Example: ParkingLot.

    If there are 8 parking spot, it can permit 8 cars to park at a single time and if 9th car
    comes it has to wait.
    When one or more cars leaves the spot then it can permit other cars to park.
    Or simply given back to semaphore for future cars to arrive.

        Semaphore is a counting semaphore.

    Example:
    Semaphore semaphore = new Semaphore(8);
    semaphore.acquire(); // Number of permits -> 1 now available.

    A thread can acquire more than one permit by passing the number of permits to the acquire method.
    example: semaphore.acquire(5); as well as semaphore.release(3);

    Difference between semaphore and thread?

    - Semaphore doesn't have a notion of ownership. i.e. it doesn't keep track of which thread acquired it last.
    - It is just a simple counter.
    - Many threads can acquire a permit.
    - The same thread can acquire the same permit multiple times without blocking.
    - The binary semaphore (initialised with 1) is not a reentrant.
    - Semaphore can be released by any thread even a thread that hasn't actually acquired it.

    If a thread acquires the semaphore 2 times it goes into the blocked state until other thread
    releases it.

    Example:
    Semaphore semaphore = new Semaphore(1);

    void function(){
        semaphore.acquire();
        semaphore.acquire(); // Thread is blocked and other thread needs to release it
    }

 */
public class Semaphore {
    // Producer - Consumer
    /*
        Semaphore full = new Semaphore(0);
        Semaphore empty = new Semaphore(1);
        Item item = null;

        Consumer:
            while(true){
                full.acquire();
                consumeItem(item);
                empty.release();
            }

        Producer:
            while(true){
                empty.acquire();
                item = produceItem();
                full.release();
            }


        The above case is happy case
        but if the consumer is faster than producer then it would spend
        most of the time in suspended mode until the items are produced

        if the consumer is slower than the producer then it will have to wait for the item to be produced.
        and it is guaranteed that producer will be waiting state until the consumer consumes it.
        To handle this case we can use a bounded buffer
     */

    /*
        Semaphore - Multiple Producers and Consumers

        Producer1 ->
                                                -> Consumer 1
        Producer 2 ->
                                QUEUE
        Producer 3 ->                           -> Consumer 2
        Producer 4 ->                           -> Consumer 3


        Semaphore full = new Semaphore(0);
        Semaphore empty = new Semaphore(CAPACITY);
        Queue queue = new ArrayDeque();
        // to protect the queue from concurrent access we can use a lock
        Lock lock = new ReentrantLock();

        Consumer:
            while(true){
                full.acquire();
                lock.lock();
                Item item = queue.poll();
                lock.unlock();
                consumeItem(item);
                empty.release();
            }

        Producer:
            while(true){
                Item item = produceItem();
                empty.acquire();
                lock.lock();
                queue.offer(item);
                lock.unlock();
                full.release();
            }



     */

}
