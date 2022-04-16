package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final Logger logger = LogManager.getLogger("App");
    static Config config = new Config(6, 2, 5);
    static ServiceDiscovery serviceDiscovery = ServiceDiscovery.getInstance();
    static ArrayBlockingQueue<Document> markUpQueue = new ArrayBlockingQueue<>(100);
    static ArrayBlockingQueue<String> urlQueue = new ArrayBlockingQueue<>(1000);

    public static void main(String[] args) {
        logger.info("App Started");
        try {
            Stream<String> lines = Files.lines(Paths.get("src/main/resources/urls.txt"));
            urlQueue.addAll(lines.collect(Collectors.toList()));
        } catch (IOException e) {
            logger.fatal(e);
        }
        checkProducers();
        checkConsumers();
    }

    synchronized public static void checkProducers() {
        for (int i = serviceDiscovery.getProducers(); i < Config.MAX_PRODUCER && !urlQueue.isEmpty(); i++) {
            new Thread(new Producer(urlQueue, markUpQueue)).start();
            if (serviceDiscovery.getProducers() >= Config.MAX_PRODUCER)
                break;
        }
    }

    synchronized public static void checkConsumers() {
        for (int i = serviceDiscovery.getConsumers(); i < Config.MAX_CONSUMER && (!markUpQueue.isEmpty() || !urlQueue.isEmpty()); i++) {
            new Thread(new Consumer(markUpQueue)).start();
            if (serviceDiscovery.getConsumers() >= Config.MAX_CONSUMER)
                break;
        }
    }
}