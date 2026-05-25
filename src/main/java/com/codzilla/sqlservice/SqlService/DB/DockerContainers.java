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
@Table(name = "docker_containers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerContainers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "database_id", nullable = false)
    private DatabaseEntity databaseId;

    @Column(name="container_id" , nullable = false)
    private String containerId;

    @Column(name = "host" , nullable = false)
    private String host;

    @Column(name = "kafka_offset")
    private Long kafkaVerdict;

    @Column(name="port")
    private Integer port;

    @Column(name="rows_matched")
    private Boolean rowsMatched;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerStatus status;

    @Column(name="last_used")
    private LocalDateTime lastUsed;

}

