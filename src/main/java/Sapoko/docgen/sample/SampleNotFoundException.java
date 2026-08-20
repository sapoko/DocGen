package Sapoko.docgen.sample;

import Sapoko.docgen.common.NotFoundException;

public class SampleNotFoundException extends NotFoundException {
    public SampleNotFoundException(long id) {
        super("Шаблон с id " + id + " не найден.");
    }
}
