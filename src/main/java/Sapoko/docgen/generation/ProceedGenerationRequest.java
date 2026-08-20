package Sapoko.docgen.generation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

@Valid
public record ProceedGenerationRequest(
        @NotNull(message = "Поле не может быть пустым")
        @Min(value = 1, message = "Поле должно быть >= 1")
        Long sampleId,

        @NotEmpty(message = "Поле не может содержать пустой список")
        List<ValueFromForm> valueFromForm,

        List<HospitalRowFromForm> hospitalRowFromForm
) {
}
