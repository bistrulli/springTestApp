package com.example.restservice;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
//import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.monitoring.v3.*;
import com.google.protobuf.util.Timestamps;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import kong.unirest.HttpResponse;
//import kong.unirest.JsonNode;
//import kong.unirest.Unirest;

// Imports for the custom metrics
import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.util.Timestamps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.*;

@RestController
public class MSController {

    private static Boolean firstRequest = TRUE;
    private static int requestCount = 0;
    private static long initialTime = System.currentTimeMillis();


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
        String projectId = "my-microservice-test-project";
        requestCount += 1;

        if(firstRequest) {
            logger.info("First request arrived. (Total: " + requestCount + ")");
            firstRequest = FALSE;
            initialTime = System.currentTimeMillis();
        }else {
            logger.info("New request arrived. (Total: " + requestCount + ")");
            long timeLapse = System.currentTimeMillis() - initialTime;
            if (timeLapse > 5000) {
                // Calculate rps
                double rps = (double) requestCount * 1000 / timeLapse;
                System.out.println("RPS = " + rps);

                // Instantiates a client
                MetricServiceClient metricServiceClient = MetricServiceClient.create();

                // Prepares an individual data point
                TimeInterval interval =
                        TimeInterval.newBuilder()
                                .setEndTime(Timestamps.fromMillis(System.currentTimeMillis()))
                                .build();
                TypedValue value = TypedValue.newBuilder().setDoubleValue(rps).build();
                Point point = Point.newBuilder().setInterval(interval).setValue(value).build();

                List<Point> pointList = new ArrayList<>();
                pointList.add(point);

                ProjectName name = ProjectName.of(projectId);

                // Prepares the metric descriptor
//                Map<String, String> metricLabels = new HashMap<>();
//                metricLabels.put("store_id", "Pittsburg");
                Metric metric =
                        Metric.newBuilder()
                                .setType("custom.googleapis.com/rps_gauge")
//                                .putAllLabels(metricLabels)
                                .build();


                // Prepares the monitored resource descriptor
                Map<String, String> resourceLabels = new HashMap<>();
                resourceLabels.put("project_id", projectId);
//                resourceLabels.put("instance_id", "gke-cluster-1-default-pool-932ec523-hn2h");
//                resourceLabels.put("zone", "northamerica-northeast1-a");
                MonitoredResource resource =
                        MonitoredResource.newBuilder().setType("global").putAllLabels(resourceLabels).build();

                // Prepares the time series request
                TimeSeries timeSeries =
                        TimeSeries.newBuilder()
                                .setMetric(metric)
                                .setResource(resource)
                                .addAllPoints(pointList)
                                .build();
                List<TimeSeries> timeSeriesList = new ArrayList<>();
                timeSeriesList.add(timeSeries);

                CreateTimeSeriesRequest request =
                        CreateTimeSeriesRequest.newBuilder()
                                .setName(name.toString())
                                .addAllTimeSeries(timeSeriesList)
                                .build();

                // Writes time series data
                metricServiceClient.createTimeSeries(request);

                logger.info("Done writing time series data.");

                metricServiceClient.close();


                // Reset values
                initialTime = System.currentTimeMillis();
                requestCount = 0;
            }

        }



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
