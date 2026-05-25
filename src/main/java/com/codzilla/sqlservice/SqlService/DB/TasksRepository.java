package com.codzilla.sqlservice.SqlService.DB;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TasksRepository  extends JpaRepository<Task, Long> {
}
