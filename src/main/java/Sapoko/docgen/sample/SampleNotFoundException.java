package Sapoko.docgen.sample;

public class SampleNotFoundException extends RuntimeException {
    public SampleNotFoundException(long id) {
        super("Шаблон с id " + id + " не найден.");
    }
}
