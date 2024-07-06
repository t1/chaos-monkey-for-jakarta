package de.codecentric.chaosmonkey.jakarta;

public record ChaosEvent(Type type, String message) {
    public enum Type {ADD, UPDATE, APPLY}
}
