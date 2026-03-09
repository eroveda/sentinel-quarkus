package com.sentinel.infra;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InterfaceManager {

    private static final Logger LOG = Logger.getLogger(InterfaceManager.class);

    @ConfigProperty(name = "sentinel.interface.mode")
    String mode;

    void onStart(@Observes StartupEvent ev) {
        LOG.infof("Sentinel Node starting in %s mode", mode);
        
        if ("WEBSOCKET".equalsIgnoreCase(mode) || "BOTH".equalsIgnoreCase(mode)) {
            // Logic to trigger the Bridge connection
        }
        
        if ("WEBHOOK".equalsIgnoreCase(mode) || "BOTH".equalsIgnoreCase(mode)) {
            LOG.info("HTTP Webhook endpoint enabled at /sentinel/check");
        }
    }
}