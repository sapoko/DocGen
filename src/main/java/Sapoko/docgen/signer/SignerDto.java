package Sapoko.docgen.signer;

public record SignerDto(
        Long id,

        String rankShort,
        String post,
        boolean active,
        BossGroup bossGroup,

        String rankGen,
        String lastNameGen,
        String givenNamesGen,

        String rankNom,
        String lastNameNom,
        String givenNamesNom,

        String rankIns,
        String lastNameIns,
        String givenNamesIns
) {
}
