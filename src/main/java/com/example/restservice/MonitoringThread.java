package com.example.restservice;

import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.monitoring.v3.*;
import com.google.protobuf.util.Timestamps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitoringThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(RestServiceApplication.class);

    public MonitoringThread() {

    }

    @Override
    public void run() {
        // Every 30 seconds send metrics
        super.run();
        double step = 30d; // Seconds
        double rps = (double) (MSController.requestCount.get() - MSController.requestCountM1.get()) /step;
        MSController.requestCountM1 = MSController.requestCount;
        try {
            logger.info("rps = {}", rps);
            writeCustomMetric("rps_gauge" ,rps);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void writeCustomMetric(String metricName, double metricValue) throws IOException {
        // Instantiates a client
        try (MetricServiceClient metricServiceClient = MetricServiceClient.create()) {

            // Prepares an individual data point
            long nowMillis = System.currentTimeMillis();
            TimeInterval interval = TimeInterval.newBuilder()
                    .setEndTime(Timestamps.fromMillis(nowMillis))
                    .setStartTime(Timestamps.fromMillis(nowMillis)) // Set startTime for clarity, even if the same as endTime
                    .build();
            TypedValue value = TypedValue.newBuilder().setDoubleValue(metricValue).build();
            Point point = Point.newBuilder().setInterval(interval).setValue(value).build();

            List<Point> pointList = new ArrayList<>();
            pointList.add(point);

            ProjectName name = ProjectName.of(Project.getProjectId());

            // Prepares the metric descriptor
            Map<String, String> metricLabels = new HashMap<>();
            String serviceName = "tier" + Project.getTierNumber();
            metricLabels.put("service", serviceName);
            Metric metric = Metric.newBuilder().setType("custom.googleapis.com/" + metricName)
                    .putAllLabels(metricLabels).build();

            // Prepares the monitored resource descriptor
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("project_id", Project.getProjectId());
            MonitoredResource resource = MonitoredResource.newBuilder().setType("global")
                    .putAllLabels(resourceLabels).build();

            // Prepares the time series request
            TimeSeries timeSeries = TimeSeries.newBuilder().setMetric(metric)
                    .setResource(resource).addAllPoints(pointList).build();

            CreateTimeSeriesRequest request = CreateTimeSeriesRequest.newBuilder()
                    .setName(name.toString())
                    .addAllTimeSeries(Collections.singletonList(timeSeries))
                    .build();

            // Writes time series data
            metricServiceClient.createTimeSeries(request);

            logger.info("Done writing time series data.");

            // metricServiceClient.close(); // No need for this as there is "try"
        }
    }
}
