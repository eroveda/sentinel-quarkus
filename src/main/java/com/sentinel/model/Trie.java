package com.sentinel.model;

public class Trie {
    private final TrieNode root = new TrieNode();

    public void insert(String pattern) {
        TrieNode current = root;
        for (char ch : pattern.toCharArray()) {
            current = current.getChildren().computeIfAbsent(ch, k -> new TrieNode());
        }
        current.setEndOfPattern(true);
    }

    /**
     * Checks if the input text contains any of the patterns stored in the Trie.
     */
    public boolean containsMatch(String text) {
        for (int i = 0; i < text.length(); i++) {
            TrieNode current = root;
            for (int j = i; j < text.length(); j++) {
                char ch = text.charAt(j);
                current = current.getChildren().get(ch);
                if (current == null) break;
                if (current.isEndOfPattern()) return true;
            }
        }
        return false;
    }
}