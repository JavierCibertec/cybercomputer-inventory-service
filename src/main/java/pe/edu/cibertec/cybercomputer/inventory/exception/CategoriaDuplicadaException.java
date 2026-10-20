package pe.edu.cibertec.cybercomputer.inventory.exception;

public class CategoriaDuplicadaException extends RuntimeException {
    public CategoriaDuplicadaException() {
        super("Ya existe una categoria con ese nombre");
    }
}
