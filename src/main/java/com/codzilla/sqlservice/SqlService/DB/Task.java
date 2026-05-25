package com.codzilla.sqlservice.SqlService.DB;




import com.codzilla.sqlservice.SqlService.model.TaskType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "database_id", nullable = false)
    private DatabaseEntity databaseId;


    @Column(name= "task_type" , nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Column(name="description")
    private String description;

    @Column(nullable = false)
    private String correctSqlResponse;

    @Column(name="time_limit_ms")
    private Integer timeLimitMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}