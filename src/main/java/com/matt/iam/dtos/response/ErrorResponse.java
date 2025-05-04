package com.matt.iam.dtos.response;

public record ErrorResponse(String error, String cause) {
    public ErrorResponse(Exception e) {
        this(e.getMessage(), e.getCause() != null ? e.getCause().toString() : "");
    }
}
