package com.codzilla.sqlservice.SqlService.DB;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatabasesRepository extends JpaRepository<DatabaseEntity, Long> {
    Optional<DatabaseEntity> findByName(String databaseName);
}
