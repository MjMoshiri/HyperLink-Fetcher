package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class Consumer implements Runnable {
    private static final Logger logger = LogManager.getLogger("Consumer");
    private static final Logger linkLogger = LogManager.getLogger("Links");
    private final ArrayBlockingQueue<Document> queue;
    private final Document poisonPill;
    private final int TIMEOUT_DURATION = 3;

    public Consumer(ArrayBlockingQueue<Document> queue, Document poisonPill) {
        this.queue = queue;
        this.poisonPill = poisonPill;
    }

    public void run() {
        try {
            logger.info("A Consumer with id " + Thread.currentThread().getId() + " started.");
            Document doc;
            Elements links;
            while (true) {
                doc = queue.poll(TIMEOUT_DURATION, TimeUnit.SECONDS);
                if (doc.equals(poisonPill)) {
                    return;
                }
                links = doc.select("a[href]");
                links.stream()
                        .map(value -> value.attr("abs:href"))
                        .filter(ref -> ref.toLowerCase().contains("http"))
                        .distinct()
                        .forEach(linkLogger::info);
            }
        } catch (Exception e) {
            logger.error("Error at thread" + Thread.currentThread().getId() + "." + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}


