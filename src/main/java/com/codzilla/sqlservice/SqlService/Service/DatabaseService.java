package com.codzilla.sqlservice.SqlService.Service;


import com.codzilla.sqlservice.SqlService.DB.DatabaseEntity;
import com.codzilla.sqlservice.SqlService.DB.DatabasesRepository;
import com.codzilla.sqlservice.SqlService.DB.Task;
import com.codzilla.sqlservice.SqlService.DB.TasksRepository;
import com.codzilla.sqlservice.SqlService.model.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {
    final private TasksRepository tasksRepository ;
    final private DatabasesRepository databases;


    @Transactional
    public Optional<Task> addTask(TaskType type , String description , String correctSqlResponse , Long databaseId) {
        Task task = new Task();
        task.setType(type);
        task.setDescription(description);
        task.setCorrectSqlResponse(correctSqlResponse);
        task.setDatabaseId(databaseId);

        Task result = tasksRepository.save(task);
        log.info("Added New Task with id :{}", result.getTask_id());

        return Optional.of(result);
    }


    @Transactional
    public Optional<DatabaseEntity> addDatabase(String name) {
        DatabaseEntity databaseEntity = new DatabaseEntity();
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
}
