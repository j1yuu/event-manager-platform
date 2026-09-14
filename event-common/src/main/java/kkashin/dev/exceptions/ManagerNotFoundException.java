package kkashin.dev.exceptions;

public class ManagerNotFoundException extends RuntimeException {
    public ManagerNotFoundException(String message) {
        super(message);
    }

    public ManagerNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }
}
