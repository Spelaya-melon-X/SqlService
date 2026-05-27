package com.codzilla.sqlservice.SqlService.Service;


import com.codzilla.sqlservice.SqlService.DB.DatabaseEntity;
import com.codzilla.sqlservice.SqlService.DB.DatabasesRepository;
import com.codzilla.sqlservice.SqlService.DB.Task;
import com.codzilla.sqlservice.SqlService.DB.TaskRepository;
import com.codzilla.sqlservice.SqlService.model.TaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {
    final private TaskRepository tasksRepository ;
    final private DatabasesRepository databases;


    @Transactional
    public Optional<Task> addTask(TaskType type , String description , String correctSqlResponse , DatabaseEntity database) {
        Task task = new Task();
        task.setType(type);
        task.setDescription(description);
        task.setCorrectSqlResponse(correctSqlResponse);
        task.setDatabase(database);

        Task result = tasksRepository.save(task);
        log.info("Added New Task with id :{}", result.getTaskId());

        return Optional.of(result);
    }


    @Transactional
    public Optional<DatabaseEntity> addDatabase(String name, @NotBlank String s) {
        DatabaseEntity databaseEntity = new DatabaseEntity();
        databaseEntity.setSchemaName(s);
        databaseEntity.setName(name);

        DatabaseEntity result = databases.save(databaseEntity);
        log.info("Added new Database with id : {}" , result.getId());
        return Optional.of(result);
    }


    @Transactional
    public void deleteTask(Long id) {
        tasksRepository.deleteById(id);
    }

    @Transactional
    public void deleteDatabaseEntity(Long id) {
        databases.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long taskId) {
        return tasksRepository.findById(taskId);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return tasksRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DatabaseEntity> getAllDatabases() {
        log.info("Fetching all databases");
        return databases.findAll();
    }

    @Transactional(readOnly = true)
    public DatabaseEntity getById(Long id) {
        return databases.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Database not found with id: " + id
                ));
    }
}
