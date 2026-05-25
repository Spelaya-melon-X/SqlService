package com.codzilla.sqlservice.SqlService.DB;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_result_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    @Column(name = "result_hash", nullable = false)
    private String resultHash;

    @Column(name = "result_json", columnDefinition = "TEXT", nullable = false)
    private String resultJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}