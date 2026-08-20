package Sapoko.docgen.signer;

import Sapoko.docgen.common.NotFoundException;

public class SignerNotFoundException extends NotFoundException {
    public SignerNotFoundException(Long id) {
        super("Подписант с id " + id + " не найден.");
    }
}
