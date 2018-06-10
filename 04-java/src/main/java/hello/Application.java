package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.ThreadExports;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.VersionInfoExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.BufferPoolsExports;


@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        // new StandardExports().register();
        // new MemoryPoolsExports().register();
        // new BufferPoolsExports().register();
        // new GarbageCollectorExports().register();
        // new ThreadExports().register();
        // new ClassLoadingExports().register();
        // new VersionInfoExports().register();

        // DefaultExports.initialize();

        SpringApplication.run(Application.class, args);
    }
}
