package pe.edu.cibertec.cybercomputer.inventory;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.cibertec.cybercomputer.inventory.model.Categoria;
import pe.edu.cibertec.cybercomputer.inventory.model.Producto;
import pe.edu.cibertec.cybercomputer.inventory.repository.CategoriaRepository;
import pe.edu.cibertec.cybercomputer.inventory.repository.ProductoRepository;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class ProductoRepositoryTest {
    @Autowired ProductoRepository productos;
    @Autowired CategoriaRepository categorias;
    @Autowired EntityManager entityManager;
    private Categoria categoria;

    @BeforeEach
    void preparar() {
        categoria = new Categoria();
        categoria.setNombre("Teclados");
        categoria = categorias.saveAndFlush(categoria);
    }

    private Producto nuevo(String nombre) {
        var p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion("Producto de prueba");
        p.setPrecio(new BigDecimal("89.90"));
        p.setStock(5);
        p.setCategoria(categoria);
        return p;
    }

    @Test
    void insertar() {
        var p = productos.saveAndFlush(nuevo("Teclado USB"));
        assertThat(p.getIdproducto()).isNotNull();
        entityManager.clear();
        var guardado = productos.findById(p.getIdproducto()).orElseThrow();
        assertThat(guardado.getNombre()).isEqualTo("Teclado USB");
        assertThat(guardado.getCategoria().getIdcategoria()).isEqualTo(categoria.getIdcategoria());
        assertThat(guardado.getPrecio()).isEqualByComparingTo("89.90");
    }

    @Test
    void actualizar() {
        var p = productos.saveAndFlush(nuevo("Teclado inicial"));
        p.setNombre("Teclado actualizado");
        p.setPrecio(new BigDecimal("99.50"));
        p.setStock(8);
        productos.saveAndFlush(p);
        entityManager.clear();
        var actualizado = productos.findById(p.getIdproducto()).orElseThrow();
        assertThat(actualizado.getNombre()).isEqualTo("Teclado actualizado");
        assertThat(actualizado.getPrecio()).isEqualByComparingTo("99.50");
        assertThat(actualizado.getStock()).isEqualTo(8);
    }

    @Test
    void eliminar() {
        var p = productos.saveAndFlush(nuevo("Producto temporal"));
        var id = p.getIdproducto();
        productos.delete(p);
        productos.flush();
        entityManager.clear();
        assertThat(productos.findById(id)).isEmpty();
    }

    @Test
    void listar() {
        productos.saveAndFlush(nuevo("Teclado uno"));
        productos.saveAndFlush(nuevo("Teclado dos"));
        entityManager.clear();
        assertThat(productos.findAll()).extracting(Producto::getNombre)
                .containsExactlyInAnyOrder("Teclado uno", "Teclado dos");
    }
}
