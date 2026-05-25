package com.codzilla.sqlservice.SqlService.model;


import lombok.Getter;

@Getter
public enum ContainerStatus {

    CREATED("Created"),
    RESTARTING("Restarting"),
    RUNNING("Running"),
    PAUSED("Paused"),
    EXITED("Exited"),
    REMOVING("Removing"),
    DEAD("Dead");

    private final String dockerValue;

    ContainerStatus(String dockerValue) {
        this.dockerValue = dockerValue;
    }

    public static ContainerStatus fromString(String status) {
        for (ContainerStatus current : ContainerStatus.values()) {
            if (current.dockerValue.equalsIgnoreCase(status)) {
                return current;
            }
        }
        throw new IllegalArgumentException("Unknown Docker container status: " + status);
    }
}
