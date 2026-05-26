package com.codzilla.sqlservice.SqlService.Conroller;

import com.codzilla.sqlservice.SqlService.DB.DockerContainers;
import com.codzilla.sqlservice.SqlService.Dto.ApiResponse;
import com.codzilla.sqlservice.SqlService.Dto.RegisterContainerRequest;
import com.codzilla.sqlservice.SqlService.Service.ContainerService;
import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sqlservice/containers")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    /**
     * POST /sqlservice/containers - Зарегистрировать Docker-контейнер с тестовой БД.
     * Вызывается при старте/деплое новых контейнеров.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DockerContainers>> register(
            @Valid @RequestBody RegisterContainerRequest req
    ) {
        DockerContainers container = containerService.register(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(container));
    }

    /**
     * GET /sqlservice/containers
     * GET /sqlservice/containers?status=RUNNING
     * Список контейнеров (опционально с фильтром по статусу).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DockerContainers>>> list(
            @RequestParam(required = false) ContainerStatus status
    ) {
        List<DockerContainers> containers = status != null
                ? containerService.getByStatus(status)
                : containerService.getAll();
        return ResponseEntity.ok(ApiResponse.ok(containers));
    }

    /**
     * PATCH /sqlservice/containers/{id}/status?value=PAUSED
     * Обновить статус контейнера (RUNNING / PAUSED / EXITED ...).
     * При переводе в не-RUNNING — DataSource вычищается из кеша.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DockerContainers>> updateStatus(
            @PathVariable Long id,
            @RequestParam ContainerStatus value
    ) {
        DockerContainers updated = containerService.updateStatus(id, value);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    /**
     * DELETE /sqlservice/containers/{id}
     * Удалить контейнер из реестра + закрыть DataSource.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        containerService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}