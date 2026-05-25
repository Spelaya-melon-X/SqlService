package com.codzilla.sqlservice.SqlService.DB;

import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import com.codzilla.sqlservice.SqlService.model.SqlVerdict;
import com.codzilla.sqlservice.SqlService.model.SubmissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;





@Entity
@Table(name = "task_result_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task taskId;

    @Column(name="result_hash" , nullable = false)
    private String resultHash;

    @Column(name="result_json" , columnDefinition = "TEXT" , nullable = false)
    private String resultJson;

    @Column(name="crated_at")
    private LocalDateTime createdAt;

}

