package com.example.restservice;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import kong.unirest.Unirest;

@SpringBootApplication
public class RestServiceApplication implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(RestServiceApplication.class);

    public static void main(String[] args) throws Exception {

        Unirest.config().concurrency(20000, 20000);
        Unirest.config().automaticRetries(false);
        Unirest.config().cacheResponses(false);
        Unirest.config().connectTimeout(0);
        Unirest.config().socketTimeout(0);

        SpringApplication.run(RestServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        logger.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        logger.info("NonOptionArgs: {}", args.getNonOptionArgs());
        logger.info("OptionNames: {}", args.getOptionNames());

        for (String name : args.getOptionNames()) {
            logger.info("arg-" + name + "=" + args.getOptionValues(name));
        }

        boolean containsOption = args.containsOption("ms.name");
        logger.info("Contains ms.name: " + containsOption);
        logger.info("Contains ms.hw: " + containsOption);
    }

}
