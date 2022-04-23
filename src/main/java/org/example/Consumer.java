package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.util.stream.Stream;


public class Consumer implements Runnable {
    private static final Logger logger = LogManager.getLogger("App");
    private static final Logger linkLogger = LogManager.getLogger("Links");

    private final Document doc;

    public Consumer(Document doc) {
        this.doc = doc;
    }

    public void run() {
        try {
            logHyperLinks(doc);
        } catch (Exception e) {
            logger.error(e);
        }
    }


    public void logHyperLinks(Document doc) {
        streamHyperLinks(doc).forEach(linkLogger::info);
    }

    public static Stream<String> streamHyperLinks(Document doc) {
        return doc.select("a[href]").stream().map(value -> value.attr("abs:href")).filter(ref -> ref.toLowerCase().startsWith("http")).distinct();
    }
}


