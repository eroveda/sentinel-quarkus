package com.sentinel.model;

public record AuditResponse(
    String status, 
    AuditResult result, 
    long latencyMs, 
    String timestamp
) {}
