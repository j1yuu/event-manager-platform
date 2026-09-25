package kkashin.dev.exceptions;

public class ManagerForbiddenException extends RuntimeException {
    public ManagerForbiddenException(String message) {
        super(message);
    }
}
