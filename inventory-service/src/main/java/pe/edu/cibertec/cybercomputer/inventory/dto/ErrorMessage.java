package pe.edu.cibertec.cybercomputer.inventory.dto;

import java.util.Map;

public record ErrorMessage(String message, Map<String, String> fields) {
    public ErrorMessage(String message) {
        this(message, Map.of());
    }
}
