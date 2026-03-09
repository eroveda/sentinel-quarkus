package com.sentinel.auditor.service;

import com.sentinel.auditor.model.AuditReport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SentinelService {
    private final ChatClient chatClient;

    public SentinelService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public AuditReport audit(String content) {
        String prompt = "Analiza este código de OpenClaw. Responde JSON: {\"score\": 0-10, \"risk\": \"LOW/HIGH\", \"details\": \"reason\"}. CONTENIDO: " + content;
        String response = chatClient.prompt(prompt).call().content();
        // Aquí iría el parser de JSON, por ahora simulamos retorno
        return new AuditReport(9.0, "HIGH", "Pattern detected via Ollama");
    }
}
