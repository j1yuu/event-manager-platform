package kkashin.dev.eventmanager.exceptions.models;

public class EMBadRequestException extends RuntimeException {
    public EMBadRequestException(String message) {
        super(message);
    }

    public EMBadRequestException(String message, Throwable ex) {
        super(message, ex);
    }
}

