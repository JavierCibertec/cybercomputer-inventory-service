package pe.edu.cibertec.cybercomputer.inventory.exception;

public class StockException extends RuntimeException {
    private final int status;
    public StockException(int status, String message) { super(message); this.status = status; }
    public int getStatus() { return status; }
}
