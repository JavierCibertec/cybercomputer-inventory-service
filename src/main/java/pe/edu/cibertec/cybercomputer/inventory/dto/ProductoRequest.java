package pe.edu.cibertec.cybercomputer.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre admite hasta 150 caracteres")
        String nombre,

        @Size(max = 1000, message = "La descripcion admite hasta 1000 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @Size(max = 500, message = "La ruta de imagen admite hasta 500 caracteres")
        String imagen,

        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "El identificador de categoria debe ser positivo")
        Integer idcategoria
) {
}
