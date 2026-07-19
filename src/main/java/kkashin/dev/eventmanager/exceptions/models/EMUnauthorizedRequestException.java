package kkashin.dev.eventmanager.exceptions.models;

public class EMUnauthorizedRequestException extends RuntimeException {
    public EMUnauthorizedRequestException(String message) {
        super(message);
    }

    public EMUnauthorizedRequestException(String message, Throwable ex) {
        super(message, ex);
    }
}
