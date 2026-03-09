package com.sentinel.service;

import com.sentinel.infra.ThreatFeedManager;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class InitializationService {

    @Inject
    ThreatFeedManager feedManager;

    void onStart(@Observes StartupEvent ev) {
        // Al arrancar, primero intentamos sincronizar. 
        // Si no hay internet, el método syncAll disparará el loadFromCache automáticamente.
        feedManager.syncAll();
    }
}