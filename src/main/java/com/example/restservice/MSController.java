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
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class MSController {
    private static final AtomicInteger requestCount = new AtomicInteger(0);
    private static long initialTime = System.currentTimeMillis();

    private static final List<Long> serviceTimesList = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(RestServiceApplication.class);


    @Value("${ms.stime}")
    private Double stime;

    @Value("${ms.name}")
    private String msname;

    private ExponentialDistribution dist = null;
    private ThreadMXBean mgm = null;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResObj msGet() throws IOException {
        logger.info("New request arrived. (Total: {})", requestCount.addAndGet(1));
        long timeLapse = System.currentTimeMillis() - initialTime;

        if (Project.getServiceName().equals("tier1")) {
            String requestedURL = "http://spring-test-app-tier2:80/";
            HttpResponse<JsonNode> resp = Unirest.get(URI.create(requestedURL).toString()).asJson();
        }

        long startTime = System.nanoTime();
        this.doWork();
        long elapsedTimeInMillis = (System.nanoTime() - startTime) / 1_000_000;
        serviceTimesList.add(elapsedTimeInMillis);

        if (timeLapse > 5000) { // More than 5 seconds passed since the counter was reset
            // Calculate rps
            double rps = (double) requestCount.get() * 1000 / timeLapse;
            logger.info("rps = {}", rps);
            writeCustomMetric( "rps_gauge", rps);
            initialTime = System.currentTimeMillis();
            requestCount.set(0);

            // Calculate average service time
            double averageServiceTime = serviceTimesList.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(Double.NaN);
            logger.info("average service time = {}ms", averageServiceTime);
            writeCustomMetric("service_time", averageServiceTime);
            serviceTimesList.clear();
        }

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

    private void writeCustomMetric(String metricName, double metricValue) throws IOException {
        // Instantiates a client
        MetricServiceClient metricServiceClient = MetricServiceClient.create();

        // Prepares an individual data point
        TimeInterval interval = TimeInterval.newBuilder().setEndTime(Timestamps.fromMillis(System.currentTimeMillis())).build();
        TypedValue value = TypedValue.newBuilder().setDoubleValue(metricValue).build();
        Point point = Point.newBuilder().setInterval(interval).setValue(value).build();

        List<Point> pointList = new ArrayList<>();
        pointList.add(point);

        ProjectName name = ProjectName.of(Project.getProjectId());

        // Prepares the metric descriptor
        Map<String, String> metricLabels = new HashMap<>();
        metricLabels.put("service", Project.getServiceName());
        Metric metric = Metric.newBuilder().setType("custom.googleapis.com/" + metricName).
                putAllLabels(metricLabels).build();


        // Prepares the monitored resource descriptor
        Map<String, String> resourceLabels = new HashMap<>();
        resourceLabels.put("project_id", Project.getProjectId());
        MonitoredResource resource = MonitoredResource.newBuilder().setType("global").putAllLabels(resourceLabels).build();

        // Prepares the time series request
        TimeSeries timeSeries = TimeSeries.newBuilder().setMetric(metric).setResource(resource).addAllPoints(pointList).build();
        List<TimeSeries> timeSeriesList = new ArrayList<>();
        timeSeriesList.add(timeSeries);

        CreateTimeSeriesRequest request = CreateTimeSeriesRequest.newBuilder().setName(name.toString()).addAllTimeSeries(timeSeriesList).build();

        // Writes time series data
        metricServiceClient.createTimeSeries(request);

        logger.info("Done writing time series data.");

        metricServiceClient.close();

    }

}
