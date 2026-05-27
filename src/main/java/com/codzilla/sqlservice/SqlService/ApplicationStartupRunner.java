package com.codzilla.sqlservice.SqlService;

import com.codzilla.sqlservice.SqlService.DB.*;
import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import com.codzilla.sqlservice.SqlService.model.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupRunner implements ApplicationRunner {

    private final DatabasesRepository databasesRepository;
    private final DockerContainersRepository containersRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Запускаем только если БД ещё пустая
        if (databasesRepository.count() > 0) {
            log.info("Data already initialized, skipping startup runner");
            return;
        }

        log.info("Initializing test databases and tasks...");

        // 1. Регистрируем тестовую БД
        DatabaseEntity testDb = new DatabaseEntity();
        testDb.setName("courier_db");
        testDb.setSchemaName("public");
        testDb = databasesRepository.save(testDb);

        // 2. Регистрируем Docker-контейнер
        // postgres-test-1 из docker-compose уже запущен на порту 5434
        DockerContainers container = DockerContainers.builder()
                .database(testDb)
                .containerId("postgres-test-1")
                .host("localhost")
                .port(5434)
                .status(ContainerStatus.RUNNING)
                .lastUsed(LocalDateTime.now())
                .build();
        containersRepository.save(container);

        // 3. Создаём тестовую задачу
        Task task = Task.builder()
                .database(testDb)
                .title("Курьеры с рейтингом выше 4.5")
                .type(TaskType.DQL)
                .description("Выбери имена всех курьеров у которых рейтинг выше 4.5, " +
                        "отсортируй по рейтингу по убыванию.")
                .correctSqlResponse(
                        "SELECT name, rating FROM couriers WHERE rating > 4.5 ORDER BY rating DESC")
                .timeLimitMs(5000)
                .build();
        taskRepository.save(task);

        log.info("Initialization complete: 1 database, 1 container, 1 task");
    }
}