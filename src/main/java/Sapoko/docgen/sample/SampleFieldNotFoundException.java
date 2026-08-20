package Sapoko.docgen.sample;

public class SampleFieldNotFoundException extends RuntimeException {
    public SampleFieldNotFoundException(Long id) {
        super("Поле шаблона с id " + id + " не найдено.");
    }
}
