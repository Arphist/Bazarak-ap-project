package com.model;

public class ApiResponse<T> {
    private T data;
    private String error;
    private int status;

    public ApiResponse() {}

    public ApiResponse(T data) {
        this.data = data;
        this.status = 200;
    }

    public ApiResponse(String error, int status) {
        this.error = error;
        this.status = status;
    }

    // Getters and Setters...
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
}