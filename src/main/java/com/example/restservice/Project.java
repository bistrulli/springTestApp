package com.example.restservice;

public class Project {
    private static final String projectId = "my-microservice-test-project";
    private static final int tierNumber = 3;

    private static final int totalTiers = 3;

    public static String getProjectId() {
        return projectId;
    }

    public static int getTierNumber() {
        return tierNumber;
    }

    public static int getTotalTiers() {
        return totalTiers;
    }

}
