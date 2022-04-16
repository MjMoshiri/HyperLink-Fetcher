package org.example;

public class Config {
    public static int MAX_PRODUCER, MAX_CONSUMER, TIMEOUT;

    public Config(int MAX_PRODUCER, int MAX_CONSUMER, int TIMEOUT) {
        Config.MAX_PRODUCER = MAX_PRODUCER;
        Config.MAX_CONSUMER = MAX_CONSUMER;
        Config.TIMEOUT = TIMEOUT;
    }
}
