package com.codzilla.sqlservice.SqlService.model;


public enum SqlVerdict {
    ACCEPTED("Решение принято"),
    COMPILATION_ERROR("Ошибка синтаксиса SQL"),
    WRONG_ANSWER("Неверный ответ"),
    TIME_LIMIT_EXCEEDED("Превышено время ожидания"),
    MEMORY_LIMIT_EXCEEDED("Превышен лимит памяти"),
    RUNTIME_ERROR("Ошибка выполнения запроса"),
    PRESENTATION_ERROR("Ошибка представления результатов"),
    SECURITY_VIOLATION("Нарушение правил безопасности"),
    SYSTEM_ERROR("Внутренняя ошибка системы");

    private final String description;

    SqlVerdict(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
