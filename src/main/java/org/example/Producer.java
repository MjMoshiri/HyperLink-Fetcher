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

public class Producer implements Runnable {
    private static final Logger logger = LogManager.getLogger("Producer");
    private static final Logger FailureLinkLogger = LogManager.getLogger("FailedLinks");
    private final ArrayBlockingQueue<String> inputQueue;
    private final ArrayBlockingQueue<Document> outputQueue;
    private final Document outPoisonPill;
    private final String inPoisonPill;

    private final int poisonPillPerProducer;
    private final int TIMEOUT_DURATION = 3;

    public Producer(ArrayBlockingQueue<String> inputQueue, ArrayBlockingQueue<Document> outputQueue, Document outPoisonPill, String inPoisonPill, int poisonPillPerProducer) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.outPoisonPill = outPoisonPill;
        this.inPoisonPill = inPoisonPill;
        this.poisonPillPerProducer = poisonPillPerProducer;
    }

    public void run() {
        try {
            logger.info("Thread started.");
            fetchMarkUp();
        } catch (InterruptedException e) {
            logger.error("Producer with id " + Thread.currentThread().getId() + " throws :" + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void fetchMarkUp() throws InterruptedException {
        String url = inputQueue.poll(TIMEOUT_DURATION, TimeUnit.SECONDS);
        Connection.Response response;
        while (!url.startsWith(inPoisonPill)) {
            try {
                response = getExecute(url);
                if (response.statusCode() == 200) {
                    while (!outputQueue.offer(response.parse(), TIMEOUT_DURATION, TimeUnit.SECONDS)) {
                        outputQueue.poll(TIMEOUT_DURATION, TimeUnit.SECONDS);
                    }
                } else {
                    FailureLinkLogger.warn("Url \"" + response.url() + "\" is Unreachable, Status code: " + response.statusCode());
                }
            } catch (HttpStatusException | HttpTimeoutException e) {
                FailureLinkLogger.warn(e.getMessage());
            } catch (SocketTimeoutException | UnknownHostException e) {
                FailureLinkLogger.warn(e.getMessage() + " Url: " + url);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            url = inputQueue.poll(TIMEOUT_DURATION, TimeUnit.SECONDS);
        }
        logger.info("Thread stopped.");
        poisonPillInserter();
    }

    private void poisonPillInserter() {
        for (int i = 0; i < poisonPillPerProducer; i++) {
            outputQueue.offer(outPoisonPill);
        }
    }

    private Connection.Response getExecute(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .timeout(5000)
                .execute();
    }
}

