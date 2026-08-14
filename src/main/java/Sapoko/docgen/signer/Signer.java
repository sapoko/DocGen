package Sapoko.docgen.signer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "signers")
@Getter
@Setter
public class Signer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String fullName;
    private String rank;
    private String post;
    private boolean active;
    private String bossGroup;

    public Signer(String fullName, String rank, String post, String bossGroup, boolean active) {
        this.fullName = fullName;
        this.rank = rank;
        this.post = post;
        this.bossGroup = bossGroup;
        this.active = active;
    }

    public Signer() {
    }
}
