package com.sathwick.learning.multithreading.threads;


/*
    Stack Memory Region:
        > Every stack holds a dedicated stack.
        > Stack is the memory region where
            > Methods are called
            > Arguments are passed
            > Local Variables are stored
            > Stack size is fixed, and relatively smaller (platform specific)
        > Stack + Instruction Pointer  = State of each thread's execution.

    Heap Memory Region
        > Shared memory region belongs to process
        > All threads can access it
        > Dynamically allocated at runtime
        > Heap size is dynamic, and relatively larger than stack
        > Heap is slower than stack
      Holds
        > Objects (anything with new operator)
        > Members of classes
        > Static variables
        > Instance variables of classes
        > Reference variables of classes (*** if not they are stored in stack)
        > Arrays
     Governed by JVM - Garbage collector.
        > Objects that are no longer referenced are deleted.
        > Members of classes - exist as long as their parent object.
        > Static variables stays forever.
     Note: References != Objects
 */

public class MemoryManagementOrDataSharing {
    /*
        Resource sharing between threads
        A resource can be something like variables, data structure, file or connection handles,
        Message or work queues

        * The resources that were stored on heap for a process between threads.

     */
}
