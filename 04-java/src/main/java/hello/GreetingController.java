package hello;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Enumeration;
import java.util.Collections;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Random;
import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.Histogram;

// import io.prometheus.client.hotspot.MemoryPoolsExports;
// import io.prometheus.client.hotspot.StandardExports;

@RestController
public class GreetingController {

    static final Counter requests = Counter.build()
        .name("ws_requests_total").help("Total requests.").register();

    /*
    static final String[] labelNames = new String[]{"method", "color"};

    static final Counter requestsL = Counter.build()
        .name("ws_requests_total_label").help("Total requests.").labelNames(labelNames).register();

    static final Gauge inprogressRequests = Gauge.build()
        .name("ws_inprogress_requests").help("Inprogress requests.").register();

    static final Summary receivedBytes = Summary.build()
        .name("ws_requests_size_bytes").help("Request size in bytes.").register();

    */
    static final Summary requestLatencyS = Summary.build()
        .name("ws_requests_latency_seconds_summary")
        .quantile(0, 0.0)   // Add 10th percentile with 1% tolerated error
        .quantile(0.25, 0.0)   // Add 10th percentile with 1% tolerated error
        .quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
        .quantile(0.75, 0.0)   // Add 90th percentile with 1% tolerated error
        .quantile(1, 0.0)   // Add 90th percentile with 1% tolerated error
        .help("Request latency in seconds.").register();
    /*
    static final Histogram requestLatencyH = Histogram.build()
        .buckets(0.00001, 0.00005, 0.0001, 0.0005, 0.001, 0.01, 0.1, 1, 2, 3)
        .name("ws_requests_latency_seconds_histo").help("Request latency in seconds.").register(); 

    */

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name",
                                           defaultValue="World") String name) {

        Summary.Timer requestTimerS = requestLatencyS.startTimer();

        /*
        Histogram.Timer requestTimerH = requestLatencyH.startTimer();

        Random rand = new Random();
        */
        // requests.inc();

        /*
        inprogressRequests.inc();
        inprogressRequests.inc();
        inprogressRequests.dec();

        requestsL.labels(new String[]{"get", "red"}).inc();
        requestsL.labels(new String[]{"post", "green"}).inc();

        receivedBytes.observe(10);
        */
        // requestTimerS.observeDuration();
        // requestTimerH.observeDuration();
       
        Greeting r = new Greeting(counter.incrementAndGet(),
                                  String.format(template, name));

        requestTimerS.observeDuration();

        return r;
    }

    @RequestMapping("/metrics")
    public String metrics() throws IOException {
        StringWriter writer = new StringWriter();

        TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
        writer.flush();
        return writer.toString();
    }
}
