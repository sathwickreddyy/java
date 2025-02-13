package com.sathwick.learning.multithreading.threads.locking;


import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.ArrayList;
import java.util.*;

/*
    Why?
    Race conditions require
        - Multiple threads sharing a resource
        - Atleast one thread is modifying the resource
    Solution:
    > Complete Mutual Exclusion
    > Regardless of operation (read/write/both)
    > Lock and allow only one thread to critical section.

    Note: Synchornized and Reentrant lock donot allow multiple readers to access
    a shared resource concurrently.
     They only allow one thread at a time to access it. In case of readers and writers
     readers can access concurrently but writers have to wait for all active readers to finish

     In the case of cache or read operations are predominant then mutual exclusion of
     reading threads could negatively impact the performance.


     How to use?

     ReentrantReadWriteLockExample lock = new ReentrantReadWriteLockExample();
     lock.readLock().lock(); or lock.writeLock().lock();

     Multiple threads can acquire the read lock.
     But only a single thread can acquire the write lock.
     and there is a mutual exclusion  between read and write locks. if there is a write
     lock acquired then no other thread can acquire the read lock until the write lock is released.

     Similarly, if there is a read lock acquired then no other thread can acquire the write lock.
 */

import java.util.TreeMap;

public class ReentrantReadWriteLockExample {
    /*
        Use case - Inventory Database
                    $6 (20items)
                    /\
                   /  \
                  4(10)15(5)items
                 /\    \
                1  5    21
              3I  8I    1000Items
        Database backed by Binary Search tree and the keys are the prices of items
        and value of each price is no. of items at that price.


        Summary:

        Using regular binary locks with read intensive workloads, prevents concurrent read from
        shared resources.
     */

    public final static int HIGHEST_PRICE = 1000;
    public static void main(String[] args) {
        InventoryDatabase inventoryDatabase = new InventoryDatabase();

        Random random = new Random();

        for(int i=0; i<10000; i++){
            inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE));
        }

        Thread writer = new Thread(()->{
            while(true){
                inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE));
                inventoryDatabase.removeItem(random.nextInt(HIGHEST_PRICE));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        writer.setDaemon(true);
        writer.start();

        int numberOfReaderThreads = 7;
        List<Thread> readers = new ArrayList<>();

        for(int i=0; i<numberOfReaderThreads; i++){
            Thread reader = new Thread(()->{
                for(int j=0; j<100000; j++){
                    int upperBound = random.nextInt(HIGHEST_PRICE);
                    int lowerBound = upperBound > 0 ? random.nextInt(upperBound) : 0;
                    inventoryDatabase.getNumberOfItemsInPriceRange(lowerBound, upperBound);
                }
            });
            reader.setDaemon(true);
            readers.add(reader);
        }

        long startReadingTime = System.currentTimeMillis();
        for (Thread reader : readers) {
            reader.start();
        }

        for(Thread reader: readers){
            try {
                reader.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endReadingTime = System.currentTimeMillis();

        System.out.println(String.format("Read operation took %d ms", endReadingTime-startReadingTime));
    }


    private static class InventoryDatabase{
        private TreeMap<Integer, Integer> priceTable = new TreeMap<>();
//        private ReentrantLock lock = new ReentrantLock(); // taking on an average of 1500 ms
        private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        // taking an average of just 600 ms which is 2.5X improvement
        private Lock readLock = lock.readLock();
        private Lock writeLock = lock.writeLock();


        public int getNumberOfItemsInPriceRange(int low, int high) {
            readLock.lock();
            try {
                Integer fromKey = priceTable.ceilingKey(low);
                Integer toKey = priceTable.floorKey(high);

                if (fromKey == null || toKey == null) return 0;

                NavigableMap<Integer, Integer> range = priceTable.subMap(fromKey, true, toKey, true);
                int count = 0;
                for (int numberOfItemsForPrice : range.values()) {
                    count += numberOfItemsForPrice;
                }
                return count;
            }
            finally {
                readLock.unlock();
            }
        }

        public void addItem(int price) {
            writeLock.lock();
            try{
                if (priceTable.containsKey(price)) {
                    priceTable.put(price, priceTable.get(price) + 1);
                } else {
                    priceTable.put(price, 1);
                }
            }
            finally {
                writeLock.unlock();
            }
        }

        public void removeItem(int price) {
            writeLock.lock();
            try {
                if (priceTable.containsKey(price)) {
                    if (priceTable.get(price) == 1) {
                        priceTable.remove(price);
                    } else {
                        priceTable.put(price, priceTable.get(price) - 1);
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }
    }

}
