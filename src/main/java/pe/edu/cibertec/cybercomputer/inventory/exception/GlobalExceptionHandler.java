package pe.edu.cibertec.cybercomputer.inventory.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.edu.cibertec.cybercomputer.inventory.dto.ErrorMessage;
import pe.edu.cibertec.cybercomputer.inventory.dto.GenericResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CategoriaDuplicadaException.class)
    public ResponseEntity<GenericResponse<Void>> categoriaDuplicada(CategoriaDuplicadaException ex) {
        return ResponseEntity.status(409).body(GenericResponse.fail(new ErrorMessage(ex.getMessage())));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<GenericResponse<Void>> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(404).body(GenericResponse.fail(new ErrorMessage(ex.getMessage())));
    }

    @ExceptionHandler(CategoriaInvalidaException.class)
    public ResponseEntity<GenericResponse<Void>> categoria(CategoriaInvalidaException ex) {
        return ResponseEntity.badRequest().body(GenericResponse.fail(
                new ErrorMessage(ex.getMessage(), Map.of("idcategoria", ex.getMessage()))));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Void>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(GenericResponse.fail(
                new ErrorMessage("Revisa los datos ingresados", fields)));
    }

    @ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<GenericResponse<Void>> parametros(Exception ex) {
        return ResponseEntity.badRequest().body(GenericResponse.fail(
                new ErrorMessage("Revisa el identificador y los datos ingresados")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse<Void>> json(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(GenericResponse.fail(
                new ErrorMessage("El cuerpo de la solicitud debe ser un JSON valido")));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse<Void>> integridad(DataIntegrityViolationException ex) {
        return ResponseEntity.status(409).body(GenericResponse.fail(
                new ErrorMessage("La operacion entra en conflicto con los datos registrados")));
    }
}
