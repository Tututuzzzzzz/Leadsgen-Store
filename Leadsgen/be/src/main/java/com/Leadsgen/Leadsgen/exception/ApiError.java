package com.Leadsgen.Leadsgen.exception;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiError {
    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    String path;
    Map<String, String> validationErrors;
}
