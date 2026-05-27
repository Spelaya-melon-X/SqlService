package com.codzilla.sqlservice.SqlService.Service;

import com.codzilla.sqlservice.SqlService.DB.DockerContainers;
import com.codzilla.sqlservice.SqlService.DB.DockerContainersRepository;
import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerDataSourceService {
    private final DockerContainersRepository containerRepository;

    private final Map<String, JdbcTemplate> jdbcTemplateCache = new ConcurrentHashMap<>();

    public JdbcTemplate getJdbcTemplate(DockerContainers container) {
        return jdbcTemplateCache.computeIfAbsent(container.getContainerId() ,
                id -> createJdbcTemplate(container));
    }


    private JdbcTemplate createJdbcTemplate(DockerContainers container) {
        log.info("Creating new DataSource for container {} at {}:{}",
                container.getContainerId(), container.getHost(), container.getPort());

        HikariConfig config = new HikariConfig();

        // JDBC URL для PostgreSQL внутри Docker-контейнера
        // host и port берём из нашей таблицы docker_containers
        String dbName = container.getDatabase().getName();
        config.setJdbcUrl(String.format(
                "jdbc:postgresql://%s:%d/%s",
                container.getHost(),
                container.getPort(),
                dbName
        ));


        //todo :  брать из application.properties
        config.setUsername("myuser");
        config.setPassword("secret");
        config.setDriverClassName("org.postgresql.Driver");

        // Маленький пул — контейнеры тестовые, не продакшн
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);

        // Таймаут соединения 5 сек — если контейнер недоступен, быстро узнаём
        config.setConnectionTimeout(5_000);

        // Имя пула для логов — удобно при дебаге
        config.setPoolName("HikariPool-" + container.getContainerId().substring(0, 8));

        // Валидация соединения перед выдачей из пула
        config.setConnectionTestQuery("SELECT 1");

        return new JdbcTemplate(new HikariDataSource(config));
    }


    /** it finds free container for DB_task and return a JdbcTemplate*/
    public JdbcTemplate getAvailableJdbcTemplate(Long databaseEntityId) {
        DockerContainers container = containerRepository
                .findFirstByDatabaseAndStatus(
                        containerRepository.findById(databaseEntityId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Database not found: " + databaseEntityId))
                                .getDatabase(),
                        ContainerStatus.RUNNING
                )
                .orElseThrow(() -> new IllegalStateException(
                        "No running containers available for database: " + databaseEntityId
                ));

        log.debug("Selected container {} ({}:{}) for database {}",
                container.getContainerId(), container.getHost(),
                container.getPort(), databaseEntityId);

        return getJdbcTemplate(container);
    }


    /** it deletes DataSource from the cache ( use when the container stop or recreate ) */
    public void evictContainer(String containerId) {
        JdbcTemplate removed = jdbcTemplateCache.remove(containerId);
        if (removed != null) {
            HikariDataSource ds = (HikariDataSource) removed.getDataSource();
            if (ds != null && !ds.isClosed()) {
                ds.close();
                log.info("Closed DataSource for container {}", containerId);
            }
        }
    }
}
