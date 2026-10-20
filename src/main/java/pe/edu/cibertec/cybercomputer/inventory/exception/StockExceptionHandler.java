package pe.edu.cibertec.cybercomputer.inventory.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.cibertec.cybercomputer.inventory.dto.*;

@RestControllerAdvice
public class StockExceptionHandler {
    @ExceptionHandler(StockException.class)
    public ResponseEntity<GenericResponse<Void>> stock(StockException ex) {
        return ResponseEntity.status(ex.getStatus()).body(GenericResponse.fail(new ErrorMessage(ex.getMessage())));
    }
}
