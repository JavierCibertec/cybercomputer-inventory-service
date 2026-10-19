package pe.edu.cibertec.cybercomputer.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.cybercomputer.inventory.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
