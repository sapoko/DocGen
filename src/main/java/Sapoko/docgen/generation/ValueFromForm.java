package Sapoko.docgen.generation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ValueFromForm(
        @NotNull(message = "Поле не может быть пустым")
        @Min(value = 1, message = "Поле должно быть >= 1")
        Long sampleFieldId,

        @Size(max = 300, message = "Размер поля должен быть <= 255")
        String value,

        @Min(value = 1, message = "Поле должно быть >= 1")
        Long signerId
) {
}
