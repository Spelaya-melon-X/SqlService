package com.codzilla.sqlservice.SqlService.DB;


import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabasesRepository extends JpaRepository<DatabaseEntity, Long> {
}
