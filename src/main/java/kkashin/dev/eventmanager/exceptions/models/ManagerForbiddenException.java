package kkashin.dev.eventmanager.exceptions.models;

public class ManagerForbiddenException extends RuntimeException {
    public ManagerForbiddenException(String message) {
        super(message);
    }
}
