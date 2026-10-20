package pe.edu.cibertec.cybercomputer.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.cybercomputer.inventory.dto.*;
import pe.edu.cibertec.cybercomputer.inventory.exception.StockException;
import pe.edu.cibertec.cybercomputer.inventory.model.Producto;
import pe.edu.cibertec.cybercomputer.inventory.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockService {
    private final ProductoRepository productos;

    @Transactional
    public List<ProductoCompraResponse> descontar(CompraRequest request) {
        var cantidades = cantidades(request);
        var bloqueados = bloquear(cantidades.keySet());
        BigDecimal total = BigDecimal.ZERO;
        for (var p : bloqueados) {
            int cantidad = cantidades.get(p.getIdproducto());
            if (p.getStock() < cantidad) { throw new StockException(409, "Stock insuficiente para " + p.getNombre()); }
            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
        }
        if (total.compareTo(new BigDecimal("999999999999.99")) > 0) {
            throw new StockException(400, "El importe del pedido excede el maximo permitido");
        }
        var resultado = new ArrayList<ProductoCompraResponse>();
        for (var p : bloqueados) {
            int cantidad = cantidades.get(p.getIdproducto());
            p.setStock(p.getStock() - cantidad);
            resultado.add(new ProductoCompraResponse(p.getIdproducto(), p.getNombre(), cantidad, p.getPrecio(), p.getStock()));
        }
        productos.flush();
        return List.copyOf(resultado);
    }

    @Transactional
    public void reponer(CompraRequest request) {
        var cantidades = cantidades(request);
        var bloqueados = bloquear(cantidades.keySet());
        for (var p : bloqueados) {
            long nuevo = (long) p.getStock() + cantidades.get(p.getIdproducto());
            if (nuevo > Integer.MAX_VALUE) { throw new StockException(409, "La reposicion excede el stock permitido"); }
        }
        bloqueados.forEach(p -> p.setStock(p.getStock() + cantidades.get(p.getIdproducto())));
        productos.flush();
    }

    private Map<Integer, Integer> cantidades(CompraRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty() || request.items().size() > 100) {
            throw new StockException(400, "El carrito debe contener entre 1 y 100 productos");
        }
        Map<Integer, Integer> cantidades = new TreeMap<>();
        for (var i : request.items()) {
            if (i == null || i.idproducto() == null || i.idproducto() <= 0
                    || i.cantidad() == null || i.cantidad() <= 0 || i.cantidad() > 9999
                    || cantidades.putIfAbsent(i.idproducto(), i.cantidad()) != null) {
                throw new StockException(400, "Cantidades invalidas o productos repetidos");
            }
        }
        return cantidades;
    }

    private List<Producto> bloquear(Set<Integer> ids) {
        return ids.stream().map(id -> productos.buscarParaActualizar(id)
                .orElseThrow(() -> new StockException(404, "Un producto del carrito no existe"))).toList();
    }
}
