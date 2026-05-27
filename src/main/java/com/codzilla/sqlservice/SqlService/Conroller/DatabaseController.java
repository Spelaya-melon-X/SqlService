package com.codzilla.sqlservice.SqlService.Conroller;


import com.codzilla.sqlservice.SqlService.DB.DatabaseEntity;
import com.codzilla.sqlservice.SqlService.Service.DatabaseService;
import com.codzilla.sqlservice.SqlService.Dto.ApiResponse;
import com.codzilla.sqlservice.SqlService.Dto.CreateDatabaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sqlservice/databases")
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseService databaseService;

    /**
     * POST /sqlservice/databases
     * Зарегистрировать новую тестовую БД (метаданные).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DatabaseEntity>> create(
            @Valid @RequestBody CreateDatabaseRequest req
    ) {
        DatabaseEntity db = databaseService.addDatabase(req.name(), req.schemaName()).orElseThrow();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(db));
    }

    /**
     * GET /sqlservice/databases
     * Список всех зарегистрированных БД.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DatabaseEntity>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(databaseService.getAllDatabases()));
    }

    /**
     * GET /sqlservice/databases/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DatabaseEntity>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(databaseService.getById(id)));
    }

    /**
     * DELETE /sqlservice/databases/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        databaseService.deleteDatabaseEntity(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}