package kkashin.dev.eventmanager.exceptions.models;

public class ManagerBadRequestException extends RuntimeException {
    public ManagerBadRequestException(String message) {
        super(message);
    }

    public ManagerBadRequestException(String message, Throwable ex) {
        super(message, ex);
    }
}

