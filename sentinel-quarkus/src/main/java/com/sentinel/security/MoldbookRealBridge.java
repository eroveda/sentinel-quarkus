package com.sentinel.security;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean; // <--- Nuevo

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.sentinel.api.DashboardResource;
import com.sentinel.model.AuditRequest;
import com.sentinel.model.AuditResult;

@ApplicationScoped
@ClientEndpoint
@Startup
public class MoldbookRealBridge extends ClientEndpointConfig.Configurator {

    @Inject
    Logger log;

    @ConfigProperty(name = "moltbook.api.key")
    String apiKey;

    // ESTÁTICO: Para que todas las instancias compartan el mismo control
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final AtomicInteger attempts = new AtomicInteger(0);
    private static final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private static Session activeSession = null; // Sesión compartida

    private final String HUB_URI = "ws://hub.moldbook.network/v4/sentinel";

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        // Reemplazá esto con tu clave real de moltbook.com
        headers.put("Authorization", List.of("Bearer" + apiKey));
        headers.put("X-Sentinel-Version", List.of("1.0.0"));
    }

    @PostConstruct
    public void init() {
        log.info("🚀 Inicializando instancia de Bridge...");
        connect();
    }

    public void connect() {
        if (isConnecting.get() || (activeSession != null && activeSession.isOpen())) {
            return;
        }

        isConnecting.set(true);
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            // Aumentamos el timeout para evitar el 'null' por timeout de handshake
            container.setDefaultMaxSessionIdleTimeout(5000);

            log.infof("🌐 [SINGLETON] Conectando a %s...", HUB_URI);

            // Intentamos la conexión
            activeSession = container.connectToServer(this, URI.create(HUB_URI));

            log.info("✅ Conexión establecida con el Hub de Moldbook");
            attempts.set(0); // Reset de reintentos si conectó
        } catch (Exception e) {
            // Mejoramos el log para saber QUÉ falló exactamente
            String errorType = e.getClass().getSimpleName();
            String errorMsg = (e.getMessage() != null) ? e.getMessage() : "Sin mensaje (posible Connection Refused)";
            log.errorf("❌ Fallo de conexión [%s]: %s", errorType, errorMsg);

            scheduleReconnection();
        } finally {
            isConnecting.set(false);
        }
    }

    @ConfigProperty(name = "sentinel.wallet.address")
    String walletAddress;

    @OnOpen
    public void onOpen(Session session) {
        activeSession = session;
        isConnecting.set(false);

        // ... luego en el announcement ...
        String announcement = String.format("""
                {
                "type": "REGISTER_SENTINEL",
                "nodeId": "Atenea-V-Lopez",
                "wallet": "%s",
                "capabilities": ["SECURITY_AUDIT"],
                "rate_per_ms": 0.1
                }
                """, walletAddress);

        session.getAsyncRemote().sendText(announcement);
        log.info("📢 Anuncio de servicio enviado al Hub. Esperando tareas...");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("🛑 Apagando Bridge y limpiando hilos...");
        scheduler.shutdownNow(); // Mata el thread pool activo
        isConnecting.set(false);
        activeSession = null;
    }

    @Inject
    com.sentinel.service.AuditorAI auditor;
    @Inject
    com.sentinel.service.WalletService wallet;
    @Inject
    com.fasterxml.jackson.databind.ObjectMapper mapper;

    @OnMessage
    public void onMessage(String message) {
        try {
            AuditRequest request = mapper.readValue(message, AuditRequest.class);
            long startTime = System.currentTimeMillis();

            // Inferencia en la 3060
            AuditResult result = auditor.analyze(request);

            long duration = System.currentTimeMillis() - startTime;
            wallet.addEarnings(duration);

            // USAR EL HELPER CENTRALIZADO (Para actualizar logs y contadores a la vez)
            DashboardResource.addLog(result);

            if (activeSession != null && activeSession.isOpen()) {
                activeSession.getAsyncRemote().sendText(mapper.writeValueAsString(result));
                log.infof("💰 Ganancia acreditada: %d ms", duration);
            }
        } catch (Exception e) {
            log.error("❌ Error procesando auditoría remota: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.warnf("🔌 Conexión cerrada: %s. Razón: %s", session.getId(), reason.getReasonPhrase());
        activeSession = null;
        scheduleReconnection();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("❌ Error en WebSocket: " + throwable.getMessage());
        // No cerramos la sesión aquí, dejamos que onClose se encargue si la conexión
        // cae
    }

    private void scheduleReconnection() {
        if (isConnecting.get())
            return;

        // Backoff exponencial: 2, 4, 8, 16... hasta un máximo de 60 segundos
        int delay = (int) Math.min(Math.pow(2, attempts.getAndIncrement()), 60);

        log.infof("⏱️ Reintento programado en %d segundos (Intento #%d)...", delay, attempts.get());

        scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }
}