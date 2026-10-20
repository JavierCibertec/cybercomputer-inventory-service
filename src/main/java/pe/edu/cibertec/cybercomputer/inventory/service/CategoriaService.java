package pe.edu.cibertec.cybercomputer.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.cybercomputer.inventory.dto.CategoriaResponse;
import pe.edu.cibertec.cybercomputer.inventory.repository.CategoriaRepository;
import java.util.List;
import pe.edu.cibertec.cybercomputer.inventory.dto.CategoriaRequest;
import pe.edu.cibertec.cybercomputer.inventory.model.Categoria;
import pe.edu.cibertec.cybercomputer.inventory.exception.CategoriaDuplicadaException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository repository;

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (repository.existsByNombreIgnoreCase(request.nombre())) throw new CategoriaDuplicadaException();
        var categoria = new Categoria();
        categoria.setNombre(request.nombre());
        categoria = repository.saveAndFlush(categoria);
        return new CategoriaResponse(categoria.getIdcategoria(), categoria.getNombre());
    }

    public List<CategoriaResponse> listar() {
        return repository.findAll(Sort.by("idcategoria")).stream()
                .map(c -> new CategoriaResponse(c.getIdcategoria(), c.getNombre())).toList();
    }
}
