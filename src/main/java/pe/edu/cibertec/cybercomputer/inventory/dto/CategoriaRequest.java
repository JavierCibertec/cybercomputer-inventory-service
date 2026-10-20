package pe.edu.cibertec.cybercomputer.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(@NotBlank @Size(max = 100) String nombre) {
    public CategoriaRequest {
        nombre = nombre == null ? null : nombre.strip();
    }
}
