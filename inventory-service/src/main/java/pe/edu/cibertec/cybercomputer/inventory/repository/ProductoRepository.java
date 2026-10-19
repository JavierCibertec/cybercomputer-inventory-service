package pe.edu.cibertec.cybercomputer.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import pe.edu.cibertec.cybercomputer.inventory.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.idproducto = :id")
    Optional<Producto> buscarParaActualizar(@Param("id") Integer id);
}
