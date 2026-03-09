# Technical Architecture: Atenea Security Node

## Overview
Atenea is designed as a decentralized security layer for the **Moldbook Network**, following the **Web 4.0** principle of autonomous intelligence.

## The Hybrid Filter Engine
1. **L1 Filter (CPU-Trie):** A high-performance trie structure implemented in Java for O(k) complexity pattern matching. This layer blocks known threats instantly without GPU overhead.
2. **L2 Filter (LLM Audit):** Semantic analysis powered by **LangChain4j** and **Ollama 3.1**. This layer utilizes a CUDA-enabled GPU (12GB VRAM optimized) to audit the "intent" of the interaction.

## Hardware & Environment
- **Runtime:** Quarkus on Java 21.
- **Operating System:** Ubuntu.
- **GPU Acceleration:** CUDA-enabled inference for real-time semantic scoring.

## Vision: Screen Freedom
By automating security audits at the edge, Atenea reduces the need for manual monitoring, aligning with the goal of **Screen Freedom**.

---
*Developed by Esteban Roveda - Software Architect & Java Dev*
