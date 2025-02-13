package com.sathwick.learning.multithreading.threads.interthreadcommunication;




import java.io.*;
import java.util.*;


/*
    Thread can have condition, if the condition is not met then it will go to sleep state
    until some other thread change the state and signals the thread A to wake up.

    - Condition variables is always associated with a lock
    - The lock ensures atomic check and modification of the shared variables, involved in the
    condition.

    Example: Producer Consumer

    - UI Thread to render the UI to take the user input for username and password
    - Another thread look up the credentials in the database and check if the credentials are
    correct or not. Looking up in DB takes long time and cannot obviously use with UI thread.
    - So we can use condition variable to notify the UI thread when the credentials are checked

    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    String username = null, password = null;

    DB Thread :
        lock.lock();
        try{
            while(username == null && password == null){
                condition.await(); // unlocks lock and puts thread to sleep.
                // now UI thread can acquire this lock once unlocked.
            }
        } finally {
            lock.unlock();
        }
        doStuff();


     UI Thread:
        lock.lock();
        try{
            username = userTextBox().getText();
            password = passwordTextBox().getText();
            condition.signal(); // DB thread wakes up but it can't perform any op as there is no
            // lock acquired by DB thread
        }
        finally {
            lock.unlock(); // Now the DB thread acquires the lock and continues the iteration in while loop
        }

      Note: await() -> unlock lock and wait until signalled.
      and the thread that wakes up has to reacquire the lock associated with condition variable.


      Other variants of await() -> awaitNanos(), awaitUntil(), awaitWhile()
      awaitNanos(long nanosTimeout) - wait no longer than nanosTimeout
      awaitUntil(Date deadline) - wait until deadline date
      await(long time, TimeUnit unit) - wait time and unit

      signalAll() - signals all the threads waiting on this condition.
 */
public class Condition {

}

/*
    wait(), notify(), notifyAll() methods are used for inter thread communication.
    These are used with synchronized keyword.

    - The object class contains the following methods:
        - public final void wait() throws InterruptedException
        - public final void notify()
        - public final void notifyAll()
    - Every java class inherits from the Object Class
    - We can use any object as a condition variable and a lock (using the synchronized keyword).

    wait() : Causes the current thread to wait until another thread invokes the notify() or
    notifyAll() method for this object.

    notify() : Wakes up a single thread that is waiting on this object's monitor. If any threads
    are waiting on this object, one of them is chosen to be awakened. The chosen thread will not
    lose ownership of the monitor.

    notifyAll() : Wakes up all threads that are waiting on this object's monitor.


    public class MySharedClass {
         private boolean isComplete = false;
         public void waitUntilComplete() {
            synchronized (this) {
                 while (!isComplete) {
                     try {
                         wait();
                     } catch (InterruptedException e) {
                         // Handle the exception
                     }
                 }
             }
         }

         public void complete() {
             synchronized (this) {
                 isComplete = true;
                 this.notify(); // Notify all waiting threads
             }
         }

    }

    Similar to Condition Variables

    Object Signalling                   vs              Conditional Variable
    ---------------------------------------------------------------
    synchronized(object){               ->              lock.lock()
    }                                   ->              lock.unlock()
    object.notify()                     ->              condition.signal()
    object.wait()                       ->              condition.await()
    object.notifyAll()                  ->              condition.signalAll()

    Here we use object as lock but in conditional we use lock's condition.

    if we used synchronized method then we don't need this.wait or object.wait

 */

class WaitNotifyNotifyAll{
    // With back pressure - maintaining the queue size to avoid memory overloading.

    private static final String INPUT_FILE = "src/main/java/com/sathwick/learning/multithreading/threads/interthreadcommunication/out/matrices";
    private static final String OUTPUT_FILE = "src/main/java/com/sathwick/learning/multithreading/threads/interthreadcommunication/out/matrices_result.txt";
    private static final int N = 10;
    public static void main(String[] args) throws IOException, FileNotFoundException {
        ThreadSafeQueue threadSafeQueue = new ThreadSafeQueue();
        File inputFile = new File(INPUT_FILE);
        File outputFile = new File(OUTPUT_FILE);

        MatricesReaderProducer matricesReaderProducer = new MatricesReaderProducer(new FileReader(inputFile), threadSafeQueue);
        MatricesMultiperConsumer matricesMultiperConsumer = new MatricesMultiperConsumer(new FileWriter(outputFile), threadSafeQueue);

        matricesReaderProducer.start();
        matricesMultiperConsumer.start();
    }



    private static class MatricesPair{
        float[][] matrix1;
        float[][] matrix2;
    }

    private static class MatricesMultiperConsumer extends Thread{
        private FileWriter writer;
        private static final int N = 10;
        private ThreadSafeQueue queue;

        public MatricesMultiperConsumer(FileWriter writer, ThreadSafeQueue queue){
            this.writer = writer;
            this.queue = queue;
        }

        private float[][] multiplyMatrices(float[][] matrix1, float[][] matrix2){
            int rows = matrix1.length;
            int cols = matrix2[0].length;
            float[][] result = new float[rows][cols];

            for(int i=0; i < rows; i++){
                for(int j=0; j < cols; j++){
                    for(int k=0; k < matrix2.length; k++){
                        result[i][j] += matrix1[i][k] * matrix2[k][j];
                    }
                }
            }

            return result;
        }

        @Override
        public void run(){
            while(true){
                MatricesPair matricesPair = queue.remove();
                // if no pair then consumer has completed consuming
                if(matricesPair == null){
                    System.out.println("No more matrices to multiply");
                    break;
                }
                float[][] result = multiplyMatrices(matricesPair.matrix1, matricesPair.matrix2);
                writeToFile(writer, result);
            }
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void writeToFile(FileWriter writer, float[][] matrix){
            try {
                for(int r=0; r<N; r++){
                    StringJoiner joiner = new StringJoiner(", ");
                    for(int c=0; c<N; c++){
                        joiner.add(String.format("%.4f", matrix[r][c]));
                    }
                    writer.write(joiner.toString() + "\n");
                }
                writer.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class MatricesReaderProducer extends Thread {
        private static final int N = 10;
        private Scanner scanner;
        private ThreadSafeQueue queue;

        public MatricesReaderProducer(FileReader reader, ThreadSafeQueue queue){
            this.scanner = new Scanner(reader);
            this.queue = queue;
        }

        @Override
        public void run(){
            while(true){
                float[][] matrix1 = readMatrix();
                float[][] matrix2 = readMatrix();
                if(matrix2 == null || matrix1 == null){
                    queue.terminate();
                    System.out.println("No more matrices to read");
                    return;
                }
                MatricesPair matricesPair = new MatricesPair();
                matricesPair.matrix1 = matrix1;
                matricesPair.matrix2 = matrix2;
                queue.add(matricesPair);
            }
        }

        private float[][] readMatrix(){
            float[][] matrix = new float[N][N];

            for(int r=0; r < N; r++){
                if(!scanner.hasNext()){
                    return null;
                }
                String[] line = scanner.nextLine().split(",");
                for(int c=0; c < N; c++) {
                    matrix[r][c] = Float.valueOf(line[c]);
                }
            }

            scanner.nextLine();
            return matrix;
        }
    }

    // Our shared threadsafe queue
    private static class ThreadSafeQueue{
        private Queue<MatricesPair> queue = new LinkedList<>();
        private static final int QUEUE_MAX_SIZE = 25;
        // This is to check the queue is empty or not, with this producer have a clearance to produce
        private boolean isEmpty = true;
        // this is to signal the consumer that producer has completed producing the resources and
        // signalling consumer to complete the process and terminate.
        private boolean isTerminate = false;

        public synchronized void add(MatricesPair matricesPair){
            if(queue.size() == QUEUE_MAX_SIZE){
                try {
                    wait(); // wait until a consumer consumes the pair
                } catch (InterruptedException e) {
                    // Handle the exception
                    e.printStackTrace();
                }
            }
            queue.add(matricesPair);
            isEmpty = false;
            notify(); // notifies the consumer if a consumer is waiting
        }

        public synchronized MatricesPair remove(){
            // consumer will remove the pair from queue
            while(isEmpty && !isTerminate){
                try {
                    wait(); // wait until a producer produces the pair
                } catch (InterruptedException e) {
                    // Handle the exception
                    e.printStackTrace();
                }
            }
            // once consumer is woken then it means that there is a pair in the queue
            if(queue.size() == 1){
                // this is the last item to be consumed from this queue
                isEmpty = true;
            }
            if(queue.isEmpty() && isTerminate){
                return null;
            }

            if(queue.size() == QUEUE_MAX_SIZE-1){
                // if least one item is consumed, we can signal the producer to produce the items.
                notifyAll();
            }

            System.out.println("Queue Size "+queue.size());

            return queue.remove();
        }

        // called by producer, and we can notifyAll consumer threads to consume the matrices
        public synchronized void terminate(){
            isTerminate = true;
            notifyAll();
        }

    }
}

/*
    Summary

    - The above example shows how to use wait() and notify() methods to implement producer-consumer
    - Also we have seen how to implement back_pressure by restricting the queue size, to avoid OutOfMemoryException.
    - We can use the same logic for any object and not just for the class Object.

 */



// Helper class to generate matrices
class MatricesGenerator {

    private static final String OUTPUT_FILE = "src/main/java/com/sathwick/learning/multithreading/threads/interthreadcommunication/out/matrices";
    private static final int N = 10;
    private static final Random random = new Random();

    private static final int NUMBER_OF_MATRIX_PAIRS = 10000;

    public static void main(String[] args) throws IOException {
        File file = new File(OUTPUT_FILE);
        FileWriter fileWriter = new FileWriter(file);
        createMatrices(fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    private static float[][] createMatrix(Random random){
        // create random float matrix of Size NxN
        float[][] matrix = new float[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                matrix[i][j] = random.nextFloat(100);
            }
        }
        return matrix;
    }

    private static void saveMatrixToFile(FileWriter fileWriter, float[][] matrix) throws IOException {
        for (int i = 0; i < N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sb.append(String.format("%.4f",matrix[i][j])).append(",");
            }
            sb.append("\n");
            fileWriter.write(sb.toString());
        }
        fileWriter.write("\n");
    }

    private static void createMatrices(FileWriter fileWriter) throws IOException {
        for (int i = 0; i < NUMBER_OF_MATRIX_PAIRS; i++) {
            float[][] matrix1 = createMatrix(random);
            float[][] matrix2 = createMatrix(random);

            saveMatrixToFile(fileWriter, matrix1);
            saveMatrixToFile(fileWriter, matrix2);
        }
    }
}
