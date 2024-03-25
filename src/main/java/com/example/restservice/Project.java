package com.example.restservice;

public class Project {
    private static final String projectId = "my-microservice-test-project";
    private static final String serviceName = "tier2";

    public static String getProjectId() {
        return projectId;
    }
    public static String getServiceName() {
        return serviceName;
    }

}
