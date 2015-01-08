package ch.chiodoni.ioleggo;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

@SpringBootApplication
@EnableCaching
public class Application {

    private static final Logger LOGGER = getLogger(Application.class);

    private static final MetricRegistry registry = new MetricRegistry();
    private static ConsoleReporter reporter;

    static {
        reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.start(10, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws Throwable {
        LOGGER.info("ioLeggo starting");
        SpringApplication.run(Application.class, args);
    }

    public static MetricRegistry metricRegistry() {
        return registry;
    }

}