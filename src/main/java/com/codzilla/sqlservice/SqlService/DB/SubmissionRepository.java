package com.codzilla.sqlservice.SqlService.DB;


import com.codzilla.sqlservice.SqlService.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<SqlSubmission, Long> {
    List<SqlSubmission> findByUserId(UUID userId) ;
    List<SqlSubmission> findByTaskId(Long taskId) ;
    List<SqlSubmission> findByStatus(SubmissionStatus status) ;

}
