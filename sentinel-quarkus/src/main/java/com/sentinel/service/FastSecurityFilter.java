package com.sentinel.service;

import com.sentinel.model.Trie;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FastSecurityFilter {
    private static final Logger LOG = Logger.getLogger(FastSecurityFilter.class);
    
    // La referencia atómica al árbol en memoria
    private final AtomicReference<Trie> trieRef = new AtomicReference<>(new Trie());

    public void rebuildTrie(Collection<String> patterns) {
        LOG.infof("Rebuilding Trie with %d patterns...", patterns.size());
        
        Trie newTrie = new Trie();
        for (String pattern : patterns) {
            newTrie.insert(pattern);
        }
        
        // El "Swap" atómico: los hilos que están auditando ahora ven el nuevo árbol
        trieRef.set(newTrie);
        LOG.info("Trie swap completed successfully.");
    }

    public boolean containsThreat(String content) {
        if (content == null || content.isBlank()) return false;
        // Buscamos en el árbol actual
        return trieRef.get().containsMatch(content.toLowerCase());
    }
}