package kkashin.dev.eventmanager.exceptions.models;

public class EMNotFoundException extends RuntimeException {
    public EMNotFoundException(String message) {
        super(message);
    }

    public EMNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }
}
