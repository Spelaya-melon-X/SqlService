package com.codzilla.sqlservice.SqlService.DB;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "databases")
@Data
public class DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @Column(name = "database_name" , nullable = false)
    private String name;

    @Column(name = "schema_name" , nullable = false)
    private String schemaName;

    @Column(name= "created_at")
    private LocalDateTime createdAt ;


}
