package ch.chiodoni.ioleggo;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

import static org.slf4j.LoggerFactory.getLogger;

@EnableAutoConfiguration
@EnableCaching
@ComponentScan
public class Application {

    private static final Logger LOGGER = getLogger(Application.class);

    public static void main(String[] args) throws Throwable {
        LOGGER.info("Application starting");
        SpringApplication.run(Application.class, args);
    }

}