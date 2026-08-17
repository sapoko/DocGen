package Sapoko.docgen.document;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(long id) {
        super("Документ с id " + id + " не найден.");
    }
}
