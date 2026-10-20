package pe.edu.cibertec.cybercomputer.inventory.dto;

import java.math.BigDecimal;

public record ProductoResponse(Integer idproducto, String nombre, String descripcion,
                               BigDecimal precio, Integer stock, String imagen,
                               CategoriaResponse categoria) {
}
