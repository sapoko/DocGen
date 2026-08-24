package Sapoko.docgen.signer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignerRequest(
        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 30, message = "Размер поля должен быть <= 30")
        String rankShort,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 255, message = "Размер поля должен быть <= 255")
        String post,

        @NotNull(message = "Поле не должно быть пустым")
        BossGroup bossGroup,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 50, message = "Размер поля должен быть <= 50")
        String rankGen,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 60, message = "Размер поля должен быть <= 60")
        String lastNameGen,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 80, message = "Размер поля должен быть <= 80")
        String givenNamesGen,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 50, message = "Размер поля должен быть <= 50")
        String rankNom,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 60, message = "Размер поля должен быть <= 60")
        String lastNameNom,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 80, message = "Размер поля должен быть <= 80")
        String givenNamesNom,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 50, message = "Размер поля должен быть <= 50")
        String rankIns,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 60, message = "Размер поля должен быть <= 60")
        String lastNameIns,

        @NotNull(message = "Поле не должно быть пустым")
        @Size(max = 80, message = "Размер поля должен быть <= 80")
        String givenNamesIns
) {
}
