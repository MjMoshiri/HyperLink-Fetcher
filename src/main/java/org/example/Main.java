package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final Logger logger = LogManager.getLogger("App");
    static Config config = new Config(6, 2, 5);
    static ArrayBlockingQueue<Runnable> markUpQueue = new ArrayBlockingQueue<>(100);
    static ArrayBlockingQueue<Runnable> urlQueue = new ArrayBlockingQueue<>(1000);

    public static void main(String[] args) {
        logger.info("App Started");
        try {
            Stream<String> lines = Files.lines(Paths.get("src/main/resources/urls.txt"));
            urlQueue.addAll(lines.map(p -> new Producer(p, urlQueue)).collect(Collectors.toList()));
        } catch (IOException e) {
            logger.fatal(e);
        }
        ThreadPoolExecutor produces = new ThreadPoolExecutor(Config.MAX_PRODUCER, Config.MAX_PRODUCER, 30, TimeUnit.SECONDS, urlQueue);
        ThreadPoolExecutor consumer = new ThreadPoolExecutor(Config.MAX_CONSUMER, Config.MAX_CONSUMER, 30, TimeUnit.SECONDS, markUpQueue);
        produces.prestartAllCoreThreads();
        consumer.prestartAllCoreThreads();

        while (!urlQueue.isEmpty() || !markUpQueue.isEmpty()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        produces.shutdown();
        consumer.shutdown();
    }

}