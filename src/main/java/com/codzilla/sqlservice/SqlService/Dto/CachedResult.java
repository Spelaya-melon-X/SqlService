package com.codzilla.sqlservice.SqlService.Dto;

import java.util.List;
import java.util.Map;

public record CachedResult(List<Map<String, Object>> rows, String hash) {}