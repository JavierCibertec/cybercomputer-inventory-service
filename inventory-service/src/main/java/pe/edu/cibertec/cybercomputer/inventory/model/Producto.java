package pe.edu.cibertec.cybercomputer.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "producto",
        indexes = @Index(name = "idx_producto_categoria", columnList = "idcategoria"),
        check = {
                @CheckConstraint(name = "ck_producto_precio", constraint = "precio > 0"),
                @CheckConstraint(name = "ck_producto_stock", constraint = "stock >= 0")
        })
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproducto")
    private Integer idproducto;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ColumnDefault("0")
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "imagen", length = 500)
    private String imagen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idcategoria", nullable = false,
            foreignKey = @ForeignKey(name = "fk_producto_categoria"))
    private Categoria categoria;
}
