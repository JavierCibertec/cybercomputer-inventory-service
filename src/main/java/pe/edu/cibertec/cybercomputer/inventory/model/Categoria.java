package pe.edu.cibertec.cybercomputer.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categoria", uniqueConstraints =
        @UniqueConstraint(name = "uk_categoria_nombre", columnNames = "nombre"))
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcategoria")
    private Integer idcategoria;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
}
