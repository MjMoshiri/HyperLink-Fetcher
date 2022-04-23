

import org.example.Consumer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


public class ConsumerTest {
    Consumer testConsumer;
    ArrayBlockingQueue<Document> queue;
    Document doc;

    @BeforeEach
    public void setUp() {
//        queue = new ArrayBlockingQueue<>(5);
//        sd = ServiceDiscovery.getInstance();
//        testConsumer = new Consumer(queue);
    }

    @Test
    public void streamHyperLinks_WrongHyper_Null() throws IOException {
        File input = new File("src/test/resources/wrongHyper.html");
        doc = Jsoup.parse(input, "UTF-8", "WrongUrl.com");
        assertEquals(Consumer.streamHyperLinks(doc).collect(Collectors.toList()), List.of());
    }

    @Test
    public void streamHyperLinks_fineHypers_List() throws IOException {
        File input = new File("src/test/resources/hyperLinks.html");
        doc = Jsoup.parse(input, "UTF-8", "WrongUrl.com");
        assertEquals(Consumer.streamHyperLinks(doc).collect(Collectors.toList()), List.of("https://github.com/"));
    }

}
