package com.codzilla.sqlservice.SqlService.Service;

import com.codzilla.sqlservice.SqlService.DB.DatabaseEntity;
import com.codzilla.sqlservice.SqlService.DB.DatabasesRepository;
import com.codzilla.sqlservice.SqlService.DB.DockerContainers;
import com.codzilla.sqlservice.SqlService.DB.DockerContainersRepository;
import com.codzilla.sqlservice.SqlService.Dto.RegisterContainerRequest;
import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerService {

    private final DockerContainersRepository containerRepository;
    private final DatabasesRepository databasesRepository;
    private final ContainerDataSourceService dataSourceService;

    @Transactional
    public DockerContainers register(RegisterContainerRequest req) {
        DatabaseEntity db = databasesRepository.findById(req.databaseId())
                .orElseThrow(() -> new IllegalArgumentException("Database not found: " + req.databaseId()));

        DockerContainers container = DockerContainers.builder()
                .database(db)
                .containerId(req.containerId())
                .host(req.host())
                .port(req.port())
                .status(ContainerStatus.RUNNING)
                .lastUsed(LocalDateTime.now())
                .build();

        DockerContainers saved = containerRepository.save(container);
        log.info("Registered container {} at {}:{}", req.containerId(), req.host(), req.port());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<DockerContainers> getAll() {
        return containerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DockerContainers> getByStatus(ContainerStatus status) {
        return containerRepository.findAllByStatus(status);
    }

    /**
     * Обновить статус контейнера (например, RUNNING → PAUSED).
     * При деактивации — вычищаем DataSource из кеша чтобы не держать мёртвые соединения.
     */
    @Transactional
    public DockerContainers updateStatus(Long containerId, ContainerStatus newStatus) {
        DockerContainers container = containerRepository.findById(containerId)
                .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));

        ContainerStatus oldStatus = container.getStatus();
        container.setStatus(newStatus);
        DockerContainers saved = containerRepository.save(container);

        boolean wasRunning = oldStatus == ContainerStatus.RUNNING;
        boolean nowStopped = newStatus != ContainerStatus.RUNNING;
        if (wasRunning && nowStopped) {
            dataSourceService.evictContainer(container.getContainerId());
        }

        log.info("Container {} status: {} → {}", containerId, oldStatus, newStatus);
        return saved;
    }

    @Transactional
    public void delete(Long containerId) {
        DockerContainers container = containerRepository.findById(containerId)
                .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));

        dataSourceService.evictContainer(container.getContainerId());
        containerRepository.delete(container);
        log.info("Deleted container {}", containerId);
    }
}