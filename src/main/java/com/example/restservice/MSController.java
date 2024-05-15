package com.example.restservice;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URI;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class MSController {
    public static AtomicInteger requestCount = new AtomicInteger(0);
    public static AtomicInteger requestCountM1 = new AtomicInteger(0); // Previous step req count
    public static AtomicInteger activeRequests = new AtomicInteger(0); // Previous step req count

    public static AtomicLong serviceTimesSum = new AtomicLong(0);
    public static AtomicLong serviceTimesSumM1 = new AtomicLong(0);

    private static final Logger logger = LoggerFactory.getLogger(RestServiceApplication.class);

    private MonitoringThread mnt = null;

    @Value("${ms.stime}")
    private Double stime;

    @Value("${ms.name}")
    private String msname;

    private ExponentialDistribution dist = null;
    private ThreadMXBean mgm = null;

    public MSController() {
        mnt = new MonitoringThread();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(mnt, 0, 30, TimeUnit.SECONDS);
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    @ResponseBody
    public String ping() {
        return "Pong";
    }


    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResObj msGet() throws IOException {
        activeRequests.incrementAndGet();

        long startTime = System.currentTimeMillis(); // TODO nanotime
        logger.info("New request arrived. (Total: {})", requestCount.addAndGet(1));

        int n = Project.getTierNumber();
        if (n != Project.getTotalTiers()) { // Any non-final tier
            // Send a request to the next tier
            String requestedURL = "http://spring-test-app-tier" + (n + 1) + ":80/";
            logger.info("Sending message to: {}", requestedURL);
            HttpResponse<JsonNode> resp = Unirest.get(URI.create(requestedURL).toString()).asJson();
        }
        this.doWork();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime; // Elapsed time in milliseconds
        logger.info("Single request service time: {} ms", elapsedTime);

        logger.info("Current serviceTimeSum: {} ms", serviceTimesSum.addAndGet(elapsedTime));
        activeRequests.decrementAndGet();
        return new ResObj();
    }


    private void doWork() {
        if (this.dist == null) this.dist = new ExponentialDistribution(this.stime);
        if (this.mgm == null) this.mgm = ManagementFactory.getThreadMXBean();

        long delay = Long.valueOf(Math.round(this.dist.sample() * 1e09));
        long start = this.mgm.getCurrentThreadCpuTime();
        while ((this.mgm.getCurrentThreadCpuTime() - start) < delay) {
        }
    }

//    @PostMapping("/preStop")
//    public ResObj preStop() {
//        logger.info("Inside preStop.");
//
//        // Wait for completion
//        while (activeRequests.get() > 0) {
//            try {
//                TimeUnit.MILLISECONDS.sleep(100);
//            } catch (InterruptedException e) {
//                // Handle interruption (optional)
//            }
//        }
//        return new ResObj();
//    }


}
