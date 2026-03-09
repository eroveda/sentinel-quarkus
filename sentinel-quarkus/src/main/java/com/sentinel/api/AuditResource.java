package com.sentinel.api;

import com.sentinel.service.AuditorAI;
import com.sentinel.service.WalletService;
import com.sentinel.service.FastSecurityFilter;
import com.sentinel.model.AuditRequest;
import com.sentinel.model.AuditResult;
import com.sentinel.filter.Protected;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/sentinel")
public class AuditResource {

    private static final Logger LOG = Logger.getLogger(AuditResource.class);

    @Inject
    FastSecurityFilter fastFilter;

    @Inject
    AuditorAI auditor;

    @Inject
    WalletService wallet;

    @POST
    @Path("/check")
    @Protected // <--- Protección por API Key activa
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AuditResult audit(AuditRequest request) {
        long startTime = System.currentTimeMillis();
        AuditResult result;

        // 1. INTENTO POR CPU (Fast Path)
        if (fastFilter.containsThreat(request.content())) {
            result = new AuditResult("CPU-Trie", "CRITICAL", 10.0, "Amenaza detectada por filtro rápido");
            
            // Actualizamos estadísticas del Dashboard (CPU)
            DashboardResource.cpuCount.incrementAndGet();
            DashboardResource.addLog(result);
            
            // Sumamos ganancia fija por proceso liviano
            wallet.addEarnings(wallet.getCpuMinMs()); 
        } 
        // 2. INTENTO POR GPU (Deep Path)
        else {
            try {
                result = auditor.analyze(request); // Inferencia en Ollama 3.1
                
                // Actualizamos estadísticas del Dashboard (GPU)
                DashboardResource.gpuCount.incrementAndGet();
                DashboardResource.addLog(result);
                
                long duration = System.currentTimeMillis() - startTime;
                wallet.addEarnings(duration); // Ganancia proporcional al esfuerzo
                
            } catch (Exception e) {
                LOG.error("Error en inferencia GPU (Ollama offline?)", e);
                result = new AuditResult("SYSTEM", "ERROR", 0.0, "IA no disponible");
            }
        }

        return result;
    }
}