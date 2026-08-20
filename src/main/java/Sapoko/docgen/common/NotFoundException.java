package Sapoko.docgen.common;

public abstract class NotFoundException extends RuntimeException {
    protected NotFoundException(String message) {
        super(message);
    }
}
