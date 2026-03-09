package com.sentinel.model;

// Un Record es inmutable, perfecto para logs de auditoría
public record AuditResult(
    String engine, 
    String risk, 
    double score, 
    String reason
) {}