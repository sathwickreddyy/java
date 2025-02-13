package com.sathwick.learning.multithreading.threads.LockFreeAlgorithmsDSAndNonBlocking;

import java.util.concurrent.atomic.AtomicReference;


/*
    AtomicReference<T>
        - AtomicReference(V initialValue)
        - V get()
        - void set(V newValue)
        - boolean compareAndSet(V expectedValue, V newValue) ****

    CompareAndSet(V expectedValue, V newValue)
        - Assigns new value if current_value == expected_value
        - Ignores the new value if the current_value != expected_value


 */

public class LockFreeDatastructure {

    public static void main(String[] args) {
        String oldName = "old name";
        String newName = "new name";
        AtomicReference<String> atomicReference = new AtomicReference<>(oldName);
        // current value = old name

        if(atomicReference.compareAndSet(oldName, newName)){
            System.out.println("Updated successfully");
        }else{
            System.out.println("Failed to update");
        }

        atomicReference.set("Unexpected Name");
        if(atomicReference.compareAndSet(oldName, newName)){
            System.out.println("Updated successfully");
        }else{
            System.out.println("Failed to update");
        }
        // helps in protecting the value by unexpectedly overridden by other thread.
        /*
        Output:
            Updated successfully
            Failed to update
         */
    }
}
