package com.sentinel.api;

import com.sentinel.service.WalletService;
import com.sentinel.model.AuditResult;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

@Path("/dashboard")
public class DashboardResource {

    // Estadísticas globales (Thread-Safe para tu RTX 3060)
    public static final AtomicInteger cpuCount = new AtomicInteger(0);
    public static final AtomicInteger gpuCount = new AtomicInteger(0);
    public static final List<AuditResult> recentLogs = new CopyOnWriteArrayList<>();

    @Inject
    Template dashboard;

    @Inject
    WalletService walletService; // <--- Inyectamos para el balance inicial

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        return dashboard
                .data("cpuBlocks", cpuCount.get())
                .data("gpuChecks", gpuCount.get())
                .data("initialBalance", walletService.getBalance()) // <--- Pasamos la plata
                .data("logs", recentLogs)
                .render();
    }

    /**
     * Helper estático para actualizar el dashboard desde otros servicios
     */
    public static void addLog(AuditResult result) {
        gpuCount.incrementAndGet();
        // Mantenemos solo los últimos 10 logs para no saturar la RAM (16GB)
        if (recentLogs.size() >= 10) {
            recentLogs.remove(0);
        }
        recentLogs.add(result);
    }
}