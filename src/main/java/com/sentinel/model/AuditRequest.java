package com.sentinel.model;

public record AuditRequest(String agentId, String actionContext, String content) {}