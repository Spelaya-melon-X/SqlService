package com.codzilla.sqlservice.SqlService.DB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskResultCacheRepository extends JpaRepository<TaskResultCache, Long> {

    // Основная операция — проверить есть ли уже кеш для задачи перед выполнением
    /** is there a cache for existing the task ?*/
    Optional<TaskResultCache> findByTask(Task task);
    Optional<TaskResultCache> findByTaskTaskId(Long taskId);
    /** is there a cache ?   */
    boolean existsByTask(Task task);
}