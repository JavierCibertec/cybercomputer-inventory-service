package pe.edu.cibertec.cybercomputer.inventory.dto;

public record GenericResponse<T>(T response, ErrorMessage error) {
    public static <T> GenericResponse<T> ok(T response) {
        return new GenericResponse<>(response, null);
    }

    public static <T> GenericResponse<T> fail(ErrorMessage error) {
        return new GenericResponse<>(null, error);
    }
}
