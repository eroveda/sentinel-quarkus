package com.sentinel.service;

import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import jakarta.inject.Inject;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ApplicationScoped
public class WalletService {
    @Inject Logger log;

    @ConfigProperty(name = "sentinel.wallet.address") 
    String binanceAddress;

    private final DoubleAdder totalBalance = new DoubleAdder();
    private final Path storagePath = Paths.get("atenea_wallet.json");

    @ConfigProperty(name = "sentinel.money.rate-per-10ms")
    double ratePer10ms;

    @ConfigProperty(name = "sentinel.money.cpu-min-ms")
    long cpuMinMs;

    @PostConstruct
    void init() {
        try {
            if (Files.exists(storagePath)) {
                String content = Files.readString(storagePath);
                totalBalance.add(Double.parseDouble(content.trim()));
                log.infof("📂 Balance persistente recuperado: %.4f MOLT", getBalance());
            }
        } catch (Exception e) {
            log.warn("⚠️ No se pudo cargar balance previo, iniciando en 0.0");
        }
    }

    @Scheduled(every = "1m")
    void autoSave() {
        try {
            Files.writeString(storagePath, String.valueOf(getBalance()));
        } catch (Exception e) {
            log.error("❌ Error en autosave: " + e.getMessage());
        }
    }

    public void addEarnings(long latencyMs) {
        if (latencyMs <= 0) return;
        double earned = (latencyMs / 10.0) * ratePer10ms;
        totalBalance.add(earned);
    }

    public double getBalance() {
        return BigDecimal.valueOf(totalBalance.sum())
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public synchronized Map<String, Object> realSettlement() {
        double amount = getBalance();
        if (amount < 100) return Map.of("status", "ERROR", "message", "Mínimo 100 MOLT para liquidar");
        
        log.infof("🚀 ENVIANDO %.2f MOLT A BINANCE (%s)", amount, binanceAddress);
        totalBalance.reset();
        autoSave();
        return Map.of("status", "SUCCESS", "txId", "base_tx_" + System.currentTimeMillis());
    }

    public long getCpuMinMs() { return cpuMinMs; }
}