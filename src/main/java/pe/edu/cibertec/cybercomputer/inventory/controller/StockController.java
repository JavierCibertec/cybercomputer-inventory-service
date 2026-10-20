package pe.edu.cibertec.cybercomputer.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.cybercomputer.inventory.dto.*;
import pe.edu.cibertec.cybercomputer.inventory.service.StockService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService stock;

    @PostMapping("/descontar")
    public GenericResponse<List<ProductoCompraResponse>> descontar(@Valid @RequestBody CompraRequest request) {
        return GenericResponse.ok(stock.descontar(request));
    }

    @PostMapping("/reponer")
    public ResponseEntity<Void> reponer(@Valid @RequestBody CompraRequest request) {
        stock.reponer(request);
        return ResponseEntity.noContent().build();
    }
}
