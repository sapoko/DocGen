package Sapoko.docgen.generation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record HospitalRowFromForm(
        @NotBlank(message = "Поле не может быть пустым")
        @Size(max = 100, message = "Размер поля должен быть <= 100")
        String fullName,

        @NotBlank(message = "Поле не может быть пустым")
        @Size(max = 150, message = "Размер поля должен быть <= 150")
        String hospitalTitle,

        @NotNull(message = "Поле не может быть пустым")
        LocalDate admittedAt,

        @NotBlank(message = "Поле не может быть пустым")
        @Size(max = 50, message = "Размер поля должен быть <= 50")
        String rank,

        @NotBlank(message = "Поле не может быть пустым")
        @Size(max = 50, message = "Размер поля должен быть <= 50")
        String platoon,

        @NotBlank(message = "Поле не может быть пустым")
        @Size(max = 100, message = "Размер поля должен быть <= 100")
        String diagnosis
) {
}
