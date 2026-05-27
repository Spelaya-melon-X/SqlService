package com.codzilla.sqlservice.SqlService.Conroller;

import com.codzilla.sqlservice.SqlService.DB.DockerContainers;
import com.codzilla.sqlservice.SqlService.Dto.ApiResponse;
import com.codzilla.sqlservice.SqlService.Service.ContainerProvisioningService;
import com.codzilla.sqlservice.SqlService.preset.DatabasePreset;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sqlservice/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ContainerProvisioningService provisioningService;

    @PostMapping("/containers/provision")
    public ResponseEntity<ApiResponse<DockerContainers>> provisionContainer(
            @RequestParam("preset") DatabasePreset preset
    ) {
        DockerContainers container = provisioningService.provisionContainer(preset);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(container));
    }
}