package com.codzilla.sqlservice.SqlService;

import com.codzilla.sqlservice.SqlService.Service.ContainerProvisioningService;
import com.codzilla.sqlservice.SqlService.Service.ContainerService;
import com.codzilla.sqlservice.SqlService.preset.DatabasePreset;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationStartupRunner implements ApplicationRunner {

    private final ContainerService containerService;
    private final ContainerProvisioningService provisioningService;

    @Override
    public void run(ApplicationArguments args) {

        containerService.deleteAllContainers();

        for (DatabasePreset preset : DatabasePreset.values()) {
            provisioningService.provisionContainer(preset);
        }
    }
}