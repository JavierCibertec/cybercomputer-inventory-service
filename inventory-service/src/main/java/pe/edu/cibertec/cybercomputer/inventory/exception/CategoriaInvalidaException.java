package pe.edu.cibertec.cybercomputer.inventory.exception;

public class CategoriaInvalidaException extends RuntimeException {
    public CategoriaInvalidaException() {
        super("La categoria seleccionada no existe");
    }
}
