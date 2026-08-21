package Sapoko.docgen.signer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Entity
@Table(name = "signers")
@Getter
@Setter
public class Signer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rankShort;
    private String post;
    private boolean active;

    @Enumerated(value = EnumType.STRING)
    private BossGroup bossGroup;

    private String rankGen;
    private String lastNameGen;
    private String givenNamesGen;

    private String rankNom;
    private String lastNameNom;
    private String givenNamesNom;

    private String rankIns;
    private String lastNameIns;
    private String givenNamesIns;

    public Signer() {
    }

    public Signer(String rankShort, String post, boolean active, BossGroup bossGroup, String rankGen, String lastNameGen, String givenNamesGen, String rankNom, String lastNameNom, String givenNamesNom, String rankIns, String lastNameIns, String givenNamesIns) {
        this.rankShort = rankShort;
        this.post = post;
        this.active = active;
        this.bossGroup = bossGroup;

        this.rankGen = rankGen;
        this.lastNameGen = lastNameGen;
        this.givenNamesGen = givenNamesGen;

        this.rankNom = rankNom;
        this.lastNameNom = lastNameNom;
        this.givenNamesNom = givenNamesNom;

        this.rankIns = rankIns;
        this.lastNameIns = lastNameIns;
        this.givenNamesIns = givenNamesIns;
    }

    public String formatFullName() {
        return getGivenNamesNom().charAt(0) + ". " + getLastNameNom();
    }

    public String formatWithInitialsIns() {
        return getLastNameIns() + " " + formatInitials();
    }

    public String formatWithInitialsNom() {
        return getLastNameNom() + " " + formatInitials();
    }

    private String formatInitials() {
        return Arrays.stream(this.getGivenNamesNom().split(" "))
                .map(str -> str.substring(0, 1))
                .map(str -> str + ".")
                .collect(Collectors.joining());
    }
}
