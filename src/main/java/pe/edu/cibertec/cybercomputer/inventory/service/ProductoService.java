package pe.edu.cibertec.cybercomputer.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.cybercomputer.inventory.dto.*;
import pe.edu.cibertec.cybercomputer.inventory.exception.CategoriaInvalidaException;
import pe.edu.cibertec.cybercomputer.inventory.exception.RecursoNoEncontradoException;
import pe.edu.cibertec.cybercomputer.inventory.model.Producto;
import pe.edu.cibertec.cybercomputer.inventory.repository.CategoriaRepository;
import pe.edu.cibertec.cybercomputer.inventory.repository.ProductoRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public List<ProductoResponse> listar() {
        return productos.findAll(Sort.by("idproducto")).stream().map(this::respuesta).toList();
    }

    public ProductoResponse obtener(Integer id) {
        return respuesta(buscar(id));
    }

    @Transactional
    public ProductoResponse registrar(ProductoRequest request) {
        var producto = new Producto();
        asignar(producto, request);
        return respuesta(productos.saveAndFlush(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        var producto = buscarBloqueado(id);
        asignar(producto, request);
        return respuesta(productos.saveAndFlush(producto));
    }

    @Transactional
    public void eliminar(Integer id) {
        productos.delete(buscarBloqueado(id));
        productos.flush();
    }

    private Producto buscarBloqueado(Integer id) {
        return productos.buscarParaActualizar(id).orElseThrow(() ->
                new RecursoNoEncontradoException("El producto no existe"));
    }

    private Producto buscar(Integer id) {
        return productos.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("El producto no existe"));
    }

    private void asignar(Producto producto, ProductoRequest request) {
        var categoria = categorias.findById(request.idcategoria())
                .orElseThrow(CategoriaInvalidaException::new);
        producto.setNombre(request.nombre().strip());
        producto.setDescripcion(normalizarOpcional(request.descripcion()));
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setImagen(normalizarOpcional(request.imagen()));
        producto.setCategoria(categoria);
    }

    private String normalizarOpcional(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private ProductoResponse respuesta(Producto p) {
        var c = p.getCategoria();
        return new ProductoResponse(p.getIdproducto(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.getImagen(),
                new CategoriaResponse(c.getIdcategoria(), c.getNombre()));
    }
}
