package Sapoko.docgen.sample;

import Sapoko.docgen.common.NotFoundException;

public class SampleFieldNotFoundException extends NotFoundException {
    public SampleFieldNotFoundException(Long id) {
        super("Поле шаблона с id " + id + " не найдено.");
    }
}
