package com.example.restservice;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.util.*;

import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.protobuf.util.Timestamps;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

// Imports for the custom metrics
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class MSController {
    public static final AtomicInteger requestCount = new AtomicInteger(0);
    public static AtomicInteger requestCountM1 = new AtomicInteger(0); // Previous step req count

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

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResObj msGet() throws IOException {

        logger.info("New request arrived. (Total: {})", requestCount.addAndGet(1));

        int n = Project.getTierNumber();

        if (n != Project.getTotalTiers()) { // Any non-final tier
            // Send a request to the next tier
            String requestedURL = "http://spring-test-app-tier" + (n + 1) + ":80/";
            logger.info("Sending message to: " + requestedURL);
            HttpResponse<JsonNode> resp = Unirest.get(URI.create(requestedURL).toString()).asJson();
        }

        this.doWork();
        return new ResObj();
    }

    @GetMapping("/mnt")
    public ResObj mnt() {
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

}
