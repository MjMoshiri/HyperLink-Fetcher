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
    private static final Logger logger = LogManager.getLogger("App");
    private static final Logger FailureLinkLogger = LogManager.getLogger("FailedLinks");
    private final ArrayBlockingQueue<Runnable> outputQueue;

    String url;

    public Producer(String url, ArrayBlockingQueue<Runnable> outputQueue) {
        this.url = url;
        this.outputQueue = outputQueue;
    }

    public void run() {
        try {
            Document doc = produceDoc(url);
            if (doc != null) {
                while (!outputQueue.offer(new Consumer(doc), Config.TIMEOUT, TimeUnit.SECONDS)) {
                    outputQueue.poll(Config.TIMEOUT, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException e) {
            logger.error(String.format("Url %s error: %s", url, e));
        }
    }

    public Document produceDoc(String url) throws InterruptedException {
        try {
            Connection.Response response = fetchPage(url);
            if (response.statusCode() == 200) {
                return response.parse();
            }
            return null;
        } catch (HttpStatusException | HttpTimeoutException | SocketTimeoutException | UnknownHostException e) {
            FailureLinkLogger.warn(String.format("Url %s error: %s", url, e));
            return null;
        } catch (Exception e) {
            logger.error(String.format("Url %s error: %s", url, e));
            return null;
        }
    }

    public static Connection.Response fetchPage(String url) throws IOException {
        return Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21").timeout(5000).execute();
    }
}

