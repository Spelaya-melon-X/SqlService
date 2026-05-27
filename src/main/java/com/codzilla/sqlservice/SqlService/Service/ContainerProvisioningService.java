package com.codzilla.sqlservice.SqlService.Service;

import com.codzilla.sqlservice.SqlService.DB.DatabaseEntity;
import com.codzilla.sqlservice.SqlService.DB.DatabasesRepository;
import com.codzilla.sqlservice.SqlService.DB.DockerContainers;
import com.codzilla.sqlservice.SqlService.DB.DockerContainersRepository;
import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import com.codzilla.sqlservice.SqlService.preset.DatabasePreset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerProvisioningService {

    private final DatabasesRepository databasesRepository;
    private final DockerContainersRepository containersRepository;

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:15");
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "secret";

    @Transactional
    public DockerContainers provisionContainer(DatabasePreset preset) {
        /** Найти или создать запись в таблице databases */
        DatabaseEntity databaseEntity = databasesRepository.findByName(preset.getDatabaseName())
                .orElseGet(() -> {
                    DatabaseEntity newDb = new DatabaseEntity();
                    newDb.setName(preset.getDatabaseName());
                    newDb.setSchemaName(preset.getSchemaName());
                    newDb.setCreatedAt(LocalDateTime.now());
                    return databasesRepository.save(newDb);
                });



        /** Запустить контейнер PostgreSQL с init-скриптом */
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withUsername(DB_USER)
                .withPassword(DB_PASSWORD)
                .withDatabaseName(preset.getDatabaseName())
                .withInitScript(preset.getInitScriptPath()); // Testcontainers сам загружает из classpath
        postgres.start();

        /** Получить реальные хост и порт (обычно localhost и случайный порт) */
        String host = postgres.getHost();
        int port = postgres.getFirstMappedPort();
        String containerId = postgres.getContainerId();

        log.info("Provisioned container {} (image: {}) for preset {}, host: {}, port: {}",
                containerId, POSTGRES_IMAGE, preset, host, port);

        /** Сохранить контейнер в БД */
        DockerContainers containerEntity = DockerContainers.builder()
                .containerId(containerId)
                .host(host)
                .port(port)
                .status(ContainerStatus.RUNNING)
                .lastUsed(LocalDateTime.now())
                .database(databaseEntity)
                .build();

        return containersRepository.save(containerEntity);
    }
}