package com.sentinel.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.IOException;

@Provider
@Protected
public class SecurityHeaderFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "moltbook.api.key", defaultValue = "none")
    String apiKey;

    @ConfigProperty(name = "sentinel.interface.mode", defaultValue = "REST")
    String interfaceMode;

    @ConfigProperty(name = "sentinel.test-mode", defaultValue = "false")
    boolean testMode;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 1. Si estamos en modo test, permitimos el paso sin validar headers
        if (testMode) {
            return; 
        }

        // 2. Validación de interfaz (Websocket vs REST)
        if ("WEBSOCKET".equalsIgnoreCase(interfaceMode)) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                .entity("Restricted: Use the MoldbookRealBridge for WebSocket communication.")
                .build());
            return;
        }

        // 3. Validación de API Key obligatoria para producción
        String authHeader = requestContext.getHeaderString("X-Sentinel-Auth");
        if (authHeader == null || !authHeader.equals(apiKey)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("Invalid or missing X-Sentinel-Auth header.")
                .build());
        }
    }
}