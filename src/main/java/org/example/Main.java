package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final Logger logger = LogManager.getLogger("Main");

    public static void main(String[] args) {
        logger.info("App Started");
        int BOUND = 100;
        int N_PRODUCERS = 4;
        int N_CONSUMERS = Runtime.getRuntime().availableProcessors();
        String inPoisonPill = "inPoisonPill";
        Document outPoisonPill = new Document("outPoisonPill");
        int poisonPillPerProducer = N_CONSUMERS / N_PRODUCERS;
        int mod = N_CONSUMERS % N_PRODUCERS;
        ArrayBlockingQueue<Document> queue = new ArrayBlockingQueue<>(BOUND);
        ArrayBlockingQueue<String> list = new ArrayBlockingQueue<>(1000);
        List<String> result;
        try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/urls.txt"))) {
            result = lines.collect(Collectors.toList());
            for (int i = -1; i < N_PRODUCERS; i++) {
                result.add(inPoisonPill);
            }
            list.addAll(result);
        } catch (IOException e) {
            logger.fatal(e);
            return;
        }


        for (int i = 1; i < N_PRODUCERS; i++) {
            new Thread(new Producer(list, queue, outPoisonPill, inPoisonPill, poisonPillPerProducer)).start();
        }

        for (int j = 0; j < N_CONSUMERS; j++) {
            new Thread(new Consumer(queue, outPoisonPill)).start();
        }

        new Thread(new Producer(list, queue, outPoisonPill, inPoisonPill, poisonPillPerProducer + mod)).start();
    }

}