package com.codzilla.sqlservice.SqlService.Conroller;


import com.codzilla.sqlservice.SqlService.DB.SqlSubmission;
import com.codzilla.sqlservice.SqlService.DB.SubmissionRepository;
import com.codzilla.sqlservice.SqlService.Dto.ApiResponse;
import com.codzilla.sqlservice.SqlService.Dto.SubmissionStatusDto;
import com.codzilla.sqlservice.SqlService.Dto.SubmitRequest;

import com.codzilla.sqlservice.SqlService.Service.SubmissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/sqlservice/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;

    /**
     * POST /sqlservice/submissions - Отправить решение на проверку.
     * @return submissionId для дальнейшего поллинга.
     * 202 ACCEPTED — обработка асинхронная, результат ещё не готов.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> submit(
            @Valid @RequestBody SubmitRequest req
    ) {
        Long submissionId = submissionService.submit(req.taskId(), req.userId(), req.userSqlQuery());
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(submissionId));
    }

    /**
     * GET /sqlservice/submissions/{id} - Статус конкретной посылки — для поллинга с фронта.
     * Фронт вызывает раз в N секунд пока status != DONE/ERROR.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubmissionStatusDto>> getStatus(
            @PathVariable Long id
    ) {
        SubmissionStatusDto status = submissionService.getStatus(id);
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    /**
     * GET /sqlservice/submissions?taskId=1
     * GET /sqlservice/submissions?userId=uuid
     * GET /sqlservice/submissions?taskId=1&userId=uuid
     *
     * Гибкая фильтрация: все посылки задачи (для рейтинга),
     * посылки конкретного пользователя, или пересечение.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SqlSubmission>>> list(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) UUID userId
    ) {
        List<SqlSubmission> result;

        if (taskId != null && userId != null) {
            result = submissionRepository.findByTaskTaskIdAndUserId(taskId, userId);
        } else if (taskId != null) {
            result = submissionRepository.findByTaskTaskId(taskId);
        } else if (userId != null) {
            result = submissionRepository.findByUserId(userId);
        } else {
            result = submissionRepository.findAll();
        }

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /sqlservice/submissions/leaderboard?taskId=1
     * Топ посылок по задаче: сначала ACCEPTED, внутри — по kafka_offset (кто раньше).
     * kafka_offset гарантирует объективный порядок поступления в очередь.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<SqlSubmission>>> leaderboard(
            @RequestParam @NotNull Long taskId
    ) {
        List<SqlSubmission> submissions = submissionRepository
                .findLeaderboardByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.ok(submissions));
    }
}