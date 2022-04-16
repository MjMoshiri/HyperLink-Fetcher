package org.example;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.example.Main.checkProducers;

public class Producer implements Runnable {
    private static final Logger logger = LogManager.getLogger("App");
    private static final Logger FailureLinkLogger = LogManager.getLogger("FailedLinks");
    private final ArrayBlockingQueue<String> inputQueue;
    private final ArrayBlockingQueue<Document> outputQueue;
    private final ServiceDiscovery serviceDiscovery = ServiceDiscovery.getInstance();

    public Producer(ArrayBlockingQueue<String> inputQueue, ArrayBlockingQueue<Document> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    public void run() {
        try {
            logger.info("Thread started.");
            serviceDiscovery.addProducer();
            while (true) {
                String url = inputQueue.poll(Config.TIMEOUT, TimeUnit.SECONDS);
                if (url == null) {
                    if (inputQueue.isEmpty()) {
                        serviceDiscovery.removeProducer();
                        logger.info("Thread stopped.");
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    Document doc = produceDoc(url);
                    if (doc != null) {
                        while (!outputQueue.offer(doc, Config.TIMEOUT, TimeUnit.SECONDS)) {
                            outputQueue.poll(Config.TIMEOUT, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            serviceDiscovery.removeProducer();
            logger.error("Producer stopped. " + e);
            checkProducers();
            Thread.currentThread().interrupt();
        }
    }

    public Document produceDoc(String url) throws InterruptedException {
        try {
            Connection.Response response = fetchPage(url);
            if (response.statusCode() == 200) {
                return response.parse();
            }
            return null;
        } catch (HttpStatusException | HttpTimeoutException e) {
            FailureLinkLogger.warn(e.getMessage());
            return null;
        } catch (SocketTimeoutException | UnknownHostException e) {
            FailureLinkLogger.warn(e.getMessage() + " Url: " + url);
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static Connection.Response fetchPage(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .timeout(5000)
                .execute();
    }
}

