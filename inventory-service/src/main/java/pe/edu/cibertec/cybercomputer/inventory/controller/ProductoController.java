package pe.edu.cibertec.cybercomputer.inventory.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.cybercomputer.inventory.dto.*;
import pe.edu.cibertec.cybercomputer.inventory.service.ProductoService;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/productos")
public class ProductoController {
    private final ProductoService service;

    @GetMapping
    public GenericResponse<List<ProductoResponse>> listar() {
        return GenericResponse.ok(service.listar());
    }

    @GetMapping("/{id}")
    public GenericResponse<ProductoResponse> obtener(@PathVariable @Positive Integer id) {
        return GenericResponse.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<GenericResponse<ProductoResponse>> registrar(
            @Valid @RequestBody ProductoRequest request) {
        var creado = service.registrar(request);
        return ResponseEntity.created(URI.create("/api/v1/productos/" + creado.idproducto()))
                .body(GenericResponse.ok(creado));
    }

    @PutMapping("/{id}")
    public GenericResponse<ProductoResponse> actualizar(@PathVariable @Positive Integer id,
                                                       @Valid @RequestBody ProductoRequest request) {
        return GenericResponse.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
