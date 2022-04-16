package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.example.Main.checkConsumers;


public class Consumer implements Runnable {
    private static final Logger logger = LogManager.getLogger("App");
    private static final Logger linkLogger = LogManager.getLogger("Links");
    private final ArrayBlockingQueue<Document> queue;

    private final ServiceDiscovery serviceDiscovery = ServiceDiscovery.getInstance();

    public Consumer(ArrayBlockingQueue<Document> queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            logger.info("Consumer started.");
            serviceDiscovery.addConsumer();
            while (true) {
                Document doc = queue.poll(Config.TIMEOUT, TimeUnit.SECONDS);
                if (doc == null) {
                    if (hasNoPendDocs()) {
                        serviceDiscovery.removeConsumer();
                        logger.info("Consumer stopped.");
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    logHyperLinks(doc);
                }
            }
        } catch (Exception e) {
            serviceDiscovery.removeConsumer();
            logger.info("Consumer stopped.");
            logger.error(e);
            checkConsumers();
            Thread.currentThread().interrupt();
        }
    }

    public boolean hasNoPendDocs() {
        if (serviceDiscovery.getProducers() == 0 && queue.isEmpty()) {
            return true;
        }
        return false;
    }

    public void logHyperLinks(Document doc) {
        streamHyperLinks(doc).
                forEach(linkLogger::info);
    }

    public static Stream<String> streamHyperLinks(Document doc) {
        return doc
                .select("a[href]")
                .stream()
                .map(value -> value.attr("abs:href"))
                .filter(ref -> ref.toLowerCase().startsWith("http"))
                .distinct();
    }
}


