package pe.edu.cibertec.cybercomputer.inventory.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.cybercomputer.inventory.model.Categoria;
import pe.edu.cibertec.cybercomputer.inventory.repository.CategoriaRepository;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
public class CategoriasInitializer implements ApplicationRunner {
    private final CategoriaRepository categoriaRepository;

    public CategoriasInitializer(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String nombre : List.of("Teclados", "Mouse", "Audifonos")) {
            if (!categoriaRepository.existsByNombre(nombre)) {
                var categoria = new Categoria();
                categoria.setNombre(nombre);
                categoriaRepository.save(categoria);
            }
        }
    }
}
