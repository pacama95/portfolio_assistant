package com.portfolio;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Quarkus application entry point
 */
@QuarkusMain
public class PortfolioApplication implements QuarkusApplication {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioApplication.class);

    public static void main(String... args) {
        Quarkus.run(PortfolioApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        logger.info("Starting Portfolio Management Application...");
        logger.info("Application is ready to accept connections");
        
        Quarkus.waitForExit();
        return 0;
    }
} 