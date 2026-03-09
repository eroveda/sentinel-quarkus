package com.sentinel.model;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<>();
    private boolean isEndOfPattern = false;

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public void setEndOfPattern(boolean endOfPattern) {
        isEndOfPattern = endOfPattern;
    }

    public boolean isEndOfPattern() {
        return isEndOfPattern;
    }
}