package Sapoko.docgen.document;

import Sapoko.docgen.common.NotFoundException;

public class DocumentNotFoundException extends NotFoundException {
    public DocumentNotFoundException(long id) {
        super("Документ с id " + id + " не найден.");
    }
}
