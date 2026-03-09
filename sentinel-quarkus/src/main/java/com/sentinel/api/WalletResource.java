package com.sentinel.api;

import com.sentinel.service.WalletService;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/wallet")
public class WalletResource {

    @Inject WalletService walletService;

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_JSON)
    public WalletBalance getBalance(@Context HttpServerRequest request) {
        checkPrivateAccess(request);
        return new WalletBalance(walletService.getBalance(), "MLDS");
    }

    @POST
    @Path("/settle")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> settle(@Context HttpServerRequest request) {
        checkPrivateAccess(request);
        
        // Ejecutamos la lógica de negocio: resetear balance y "enviar" a Solana
        Map<String,Object> amount = walletService.realSettlement();
        
        return Map.of(
            "status", "SIMULATED_SUCCESS",
            "amount", amount,
            "wallet", "TuDireccionDeSolana...",
            "txId", "sim_" + System.currentTimeMillis()
        );
    }

    private void checkPrivateAccess(HttpServerRequest request) {
        String remoteAddr = request.remoteAddress().host();
        if (!"127.0.0.1".equals(remoteAddr) && !"::1".equals(remoteAddr)) {
            throw new ForbiddenException("Access restricted to node owner.");
        }
    }

    public record WalletBalance(double amount, String currency) {}
}