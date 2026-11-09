package hu.bme.backend.exceptions;

public class DuplicateResourcesException extends RuntimeException {
    public DuplicateResourcesException(String message) {
        super(message);
    }
}
