package pe.edu.cibertec.cybercomputer.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.cibertec.cybercomputer.inventory.dto.CategoriaResponse;
import pe.edu.cibertec.cybercomputer.inventory.dto.GenericResponse;
import pe.edu.cibertec.cybercomputer.inventory.service.CategoriaService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pe.edu.cibertec.cybercomputer.inventory.dto.CategoriaRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categorias")
public class CategoriaController {
    private final CategoriaService service;

    @PostMapping
    public ResponseEntity<GenericResponse<CategoriaResponse>> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(201).body(GenericResponse.ok(service.crear(request)));
    }

    @GetMapping
    public GenericResponse<List<CategoriaResponse>> listar() {
        return GenericResponse.ok(service.listar());
    }
}
