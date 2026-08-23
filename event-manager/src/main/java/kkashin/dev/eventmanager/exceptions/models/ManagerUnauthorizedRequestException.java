package kkashin.dev.eventmanager.exceptions.models;

public class ManagerUnauthorizedRequestException extends RuntimeException {
    public ManagerUnauthorizedRequestException(String message) {
        super(message);
    }

    public ManagerUnauthorizedRequestException(String message, Throwable ex) {
        super(message, ex);
    }
}
