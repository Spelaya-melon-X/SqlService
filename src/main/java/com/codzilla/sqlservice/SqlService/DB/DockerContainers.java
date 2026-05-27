package com.codzilla.sqlservice.SqlService.DB;

import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "docker_containers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerContainers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "database_id", nullable = false)
    private DatabaseEntity database;

    @Column(name = "container_id", nullable = false)
    private String containerId;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerStatus status;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;


}