package org.example;

public class ServiceDiscovery {
    Integer producers, consumers;
    static ServiceDiscovery sd = new ServiceDiscovery();

    private ServiceDiscovery() {
        producers = 0;
        consumers = 0;
    }

    public static ServiceDiscovery getInstance() {
        return sd;
    }

    synchronized public int getProducers() {
        return producers;
    }

    synchronized public void addProducer() {
        producers++;
    }

    synchronized public void removeProducer() {
        producers--;
    }

    synchronized public int getConsumers() {
        return consumers;
    }

    synchronized public void addConsumer() {
        consumers++;
    }

    synchronized public void removeConsumer() {
        consumers--;
    }
}
