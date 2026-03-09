package com.sentinel;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.eclipse.microprofile.config.ConfigProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class SentinelIntegrationTest {

    @Test
    public void testJailbreakDetectionLocal() {
        // Testeamos el flujo del CPU-Trie con una palabra de tu local_threats.txt
        given()
          .contentType(ContentType.JSON)
          .body("{\"content\": \"I want to attempt a jailbreak\"}")
          .when()
          .post("/sentinel/check")
          .then()
          .statusCode(200)
          .body("engine", is("CPU-Trie"))
          .body("risk", is("CRITICAL"));
    }

    @Test
    public void testCleanContentGpuInference() {
        // Este test debería pasar al AuditorAI (Ollama 3.1)
        given()
          .contentType(ContentType.JSON)
          .body("{\"content\": \"How do I secure my Solana wallet?\"}")
          .when()
          .post("/sentinel/check")
          .then()
          .statusCode(200)
          .body("engine", containsString("GPU")); // Valida que el flujo llegue a la 3060
    }
}