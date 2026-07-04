package com.example.incidents.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<FieldErrorDetail> details
) {
}
