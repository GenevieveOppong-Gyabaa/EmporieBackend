package com.mobiledev.emporio.dto;

public class ApiError {
    private String message;

    public ApiError() {}

    public ApiError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 