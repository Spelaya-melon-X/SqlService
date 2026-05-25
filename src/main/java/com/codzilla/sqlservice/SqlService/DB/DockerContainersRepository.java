package com.codzilla.sqlservice.SqlService.DB;

import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DockerContainersRepository extends JpaRepository<DockerContainers, Long> {

    /** find first free container */
    Optional<DockerContainers> findFirstByDatabaseIdAndStatus(DatabaseEntity database, ContainerStatus status);

    /** all containers*/
    List<DockerContainers> findAllByDatabase(DatabaseEntity database);

    /** all containers with status */
    List<DockerContainers> findAllByStatus(ContainerStatus status);
}