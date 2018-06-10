package hello;

import java.lang.System;
import java.util.Random;
import io.prometheus.client.Counter;

public class Greeting {
    private final long id;
    private final String content;
    Random rand = new Random();
    
    static final Counter requests = Counter.build()
        .name("greeting_count").help("Total greeting requests.").register();

    static final String[] labelNames = new String[]{"speed", "even"};
    static final Counter requestsL = Counter.build()
        .name("greeting_count_label")
        .help("Total greeting requests by speed")
        .labelNames(labelNames)
        .register();

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;

        try {
            long t = (long)(rand.nextFloat()*1000);
            System.err.println(t);
            Thread.sleep(t);

            String sEven = "no";
            if (t%2 == 0) {
                sEven = "yes";
            }

            if (t<250) {
                requestsL.labels(new String[]{"fast", sEven}).inc();
            } else {
                requestsL.labels(new String[]{"slow", sEven}).inc();
            }

        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        requests.inc();

    }

    public long getCounter() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
