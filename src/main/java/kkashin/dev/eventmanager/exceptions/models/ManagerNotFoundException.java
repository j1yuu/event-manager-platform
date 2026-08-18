package kkashin.dev.eventmanager.exceptions.models;

public class ManagerNotFoundException extends RuntimeException {
    public ManagerNotFoundException(String message) {
        super(message);
    }

    public ManagerNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }
}
