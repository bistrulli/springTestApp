package com.example.restservice;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URI;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

@RestController
public class MSController {

    private static final Logger logger = LoggerFactory.getLogger(RestServiceApplication.class);

    @Value("${ms.stime}")
    private Double stime;

    @Value("${ms.name}")
    private String msname;

    private ExponentialDistribution dist = null;
    private ThreadMXBean mgm = null;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResObj msGet() {

        logger.info("New request arrived.");

        // faccio la richiesta
		String requestedURL = "http://%s:%d%s".formatted(new Object[] { "spring-test-app-tier2", 80, "/" });
        //String requestedURL = "http://localhost:80";
		HttpResponse<JsonNode> resp = Unirest.get(URI.create(requestedURL).toString()).asJson();


        this.doWork();
        return new ResObj();
    }

    @GetMapping("/mnt")
    public ResObj mnt() {
        return new ResObj();
    }

    private void doWork() {
        if (this.dist == null)
            this.dist = new ExponentialDistribution(this.stime);
        if (this.mgm == null)
            this.mgm = ManagementFactory.getThreadMXBean();

        long delay = Long.valueOf(Math.round(this.dist.sample() * 1e09));
        long start = this.mgm.getCurrentThreadCpuTime();
        while ((this.mgm.getCurrentThreadCpuTime() - start) < delay) {
        }
    }

}
