package Sapoko.docgen.signer;

public class SignerNotFoundException extends RuntimeException {
    public SignerNotFoundException(Long id) {
        super("Подписант с id " + id + " не найден.");
    }
}
