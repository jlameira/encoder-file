package io.github.jlameira;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages="io.github.jlameira")
public class Application {

    public static final Logger logger = LogManager.getLogger(Application.class);
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).run(args);
    }
}