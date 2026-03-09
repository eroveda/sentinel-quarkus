# Sentinel-Quarkus: Atenea Security Node

Sentinel-Quarkus is a high-performance security auditing node designed for the **Moldbook Network**. Built with **Quarkus** and **Java 21**, it implements a hybrid threat detection architecture.

## 🚀 Vision: Web 4.0 & Screen Freedom
Following the **Web 4.0 concept by Conway**, this project aims to create a decentralized, autonomous security layer.

## 🛠 Technical Architecture
- **Fast Filtering (L1):** High-speed **CPU-Trie** for immediate pattern matching.
- **Deep Analysis (L2):** Semantic auditing via **LangChain4j** and **Ollama 3.1**.

## 🚀 Quick Start & Usage

### 1. Prerequisites
- Java 21 & Maven.
- Ollama running locally (`ollama serve`).
- NVIDIA Drivers with CUDA support.

### 2. Configuration
Copy the template and fill in your Moldbook credentials:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

### 3. Run and Test
```bash
./mvnw quarkus:dev
```
The node will start auditing the configured feed. You can perform a manual check via:
```bash
curl -X POST http://localhost:8080/audit -d '{"content": "test message"}'
```

## 💻 Hardware Requirements
- **OS:** Ubuntu.
- **GPU:** CUDA-enabled (12GB VRAM recommended).
- **RAM:** 16GB.

## 🗺️ Roadmap
- [ ] **Q2 2026**: Integration with Moldbook Identity Protocol.
- [ ] **Q3 2026**: Advanced Semantic Auditing.
- [ ] **Goal**: Full "Screen Freedom" through autonomous orchestration.

## ⚖ License
Apache License 2.0.

---
*Developed by Esteban Roveda - Software Architect & Java Dev*
