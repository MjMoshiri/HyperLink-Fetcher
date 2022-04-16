
import org.example.Producer;
import org.example.ServiceDiscovery;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProducerTest {
    Producer testProducer;
    ServiceDiscovery serviceDiscovery;
    ArrayBlockingQueue<Document> outputQueue;
    ArrayBlockingQueue<String> inputQueue;
    Document doc;

    @BeforeEach
    public void setUp() {
        outputQueue = new ArrayBlockingQueue<>(5);
        inputQueue = new ArrayBlockingQueue<>(5);
        serviceDiscovery = ServiceDiscovery.getInstance();
        testProducer = new Producer(inputQueue, outputQueue);
    }

    @Test
    public void run_HttpThrowException_null() throws Exception {
        doc = testProducer.produceDoc("");
        assertNull(doc);
        //also need to assert the log file
    }
}
