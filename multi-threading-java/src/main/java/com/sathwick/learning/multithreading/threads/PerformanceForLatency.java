package com.sathwick.learning.multithreading.threads;

import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/*
    Application:- High speed trading systems
            -> They have a lot of threads
            -> They need to monitor and control the threads

            Buy Req   ---->                     -----> Purchase
                                Application
            Sell Req  ---->                     -----> Sale
            Trade Req ---->                     -----> Trade
                          |--------Latency--------|

                Latency: Time taken to complete a task


    Latency is an important factor in performance optimization
        - measured in time to completion of a task. measure in units of time.
        - How long does it take to complete a task?

    Questions to be asked?
    -> How many subtasks/threads to break the original thread?
    -> Does break original task and aggregating results come for free?
    -> Can we break any task into subtasks?
        - No, there are 3 types of tasks
          1. Parallelizable tasks which can broken into sub-tasks to run in parallel
          2. Unbreakable, Sequential tasks which cannot be broken - Single threaded
          3. Partially parallelizable and partially sequential tasks.


    Notes:
    - # Threads = # Cores is optimal only if all threads are runnable and can run without interruption
    (no IO/blocking calls/ sleep etc.).
    - The assumption is nothing else is running that consumes a lot of CPU.
    - Hyper cores : Hyper threading -> virtual cores: a single core can run more than 1 thread at a time achieved
    through something like virtual cores.

    **Cost of parallelization and aggregation?**
         Breaking task into multiple tasks
         +
         Thread creation, passing tasks to threads
         +
         Time between thread.start() to thread getting scheduled
         +
         Time until the last thread finishes and signals
         +
         Time until the aggregating thread runs
         +
         Aggregating of the sub results into a single artifact

 */
public class PerformanceForLatency {
    public static final String SOURCE_FILE = "src/main/java/com/sathwick/learning/multithreading/resources/many-flowers.jpg";
    public static final String DEST_FILE = "src/main/java/com/sathwick/learning/multithreading/out/many-flowers-fx.jpg";

    /*
        Use case: Image Processing

        Image processing Seq vs Image Processing multi-threaded and performance/latency measurement.
     */
    public static void main(String[] args) throws IOException {
        // Recoloring algorithm
        BufferedImage originalImage = ImageIO.read(new File(SOURCE_FILE));
        BufferedImage resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        long startTime = System.currentTimeMillis();

        recolorSingleThreaded(originalImage, resultImage);
//        recolorMultiThreaded(originalImage, resultImage, 2);
        long endTime = System.currentTimeMillis();

        System.out.println("Single Threaded Solution Latency "+ String.valueOf(endTime-startTime));


        for(int i=1; i<9; i++){
            startTime = System.currentTimeMillis();

            recolorMultiThreaded(originalImage, resultImage, i);

            endTime = System.currentTimeMillis();

            System.out.println("Multi Threaded Solution: with "+i+" Thread Latency "+ String.valueOf(endTime-startTime));
        }

        ImageIO.write(resultImage, "jpg", new File(DEST_FILE));
    }

    /*
        Partition the image into NxN blocks and assign a thread to each block
     */
    public static void recolorMultiThreaded(BufferedImage originalImage, BufferedImage resultImage, int numberOfThreads){
        List<Thread> threads = new ArrayList<>();

        int width = originalImage.getWidth();
        // partition height into n pieces
        int height = originalImage.getHeight() / numberOfThreads;

        for(int i=0; i<numberOfThreads; i++) {
            final int threadMultiplier = i;

            Thread thread = new Thread(() -> {
                int leftCorner = 0;
                int topCorner = height * threadMultiplier;
                recolorImage(originalImage, resultImage, leftCorner, topCorner, width, height);
            });

            threads.add(thread);
        }

        for(Thread thread: threads){
            thread.start();
        }

        // Wait for each thread to finish
        for(Thread thread: threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void recolorSingleThreaded(BufferedImage originalImage, BufferedImage resultImage){
        recolorImage(originalImage, resultImage, 0, 0, originalImage.getWidth(), originalImage.getHeight());
    }

    public static void recolorImage(BufferedImage originalImage, BufferedImage resultImage, int leftCorner, int topCorner, int width, int height){
        for(int x = leftCorner; x<leftCorner+width && x< originalImage.getWidth(); x++){
            for(int y=topCorner; y<topCorner+height && y<originalImage.getHeight(); y++){
                recolorPixel(originalImage, resultImage, x, y);
            }
        }
    }

    public static void recolorPixel(BufferedImage originalImage, BufferedImage resultImage, int x, int y) {
        int rgb = originalImage.getRGB(x, y);

        int red = getRed(rgb);
        int green = getGreen(rgb);
        int blue = getBlue(rgb);

        int newRed, newGreen, newBlue;

        if (isShadeOfGray(red, green, blue)) {
            newRed = Math.min(255, red + 10);
            newGreen = Math.max(0, green - 80);
            newBlue = Math.max(0, blue - 20);
        } else {
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }

        int newRGB = createRGBFromColors(newRed, newGreen, newBlue);
        setRBG(resultImage, x, y, newRGB);
    }

    public static void setRBG(BufferedImage image, int x, int y, int rgb) {
        image.getRaster().setDataElements(x, y, image.getColorModel().getDataElements(rgb, null));
    }

    public static boolean isShadeOfGray(int red, int green, int blue){
        return Math.abs(getRed(red) - getGreen(green)) < 30
                && Math.abs(getRed(red) - getBlue(blue)) < 30
                && Math.abs(getGreen(green) - getBlue(blue)) < 30;
    }

    public static int createRGBFromColors(int red, int green, int blue) {
        int rgb = 0;
        rgb |= red << 16;
        rgb |= green << 8;
        rgb |= blue;

        rgb |= 0xff000000;

        return rgb;
    }

    public static int getRed(int rgb) {
        return (rgb & 0x00FF0000) >> 16;
    }

    public static int getGreen(int rgb) {
        return (rgb & 0x0000FF00) >> 8;
    }

    public static int getBlue(int rgb) {
        return rgb & 0x000000FF;
    }

}
