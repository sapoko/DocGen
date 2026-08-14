package Sapoko.docgen.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String passwordHash;
    private boolean active;

    public User(String username, String passwordHash, boolean active) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    public User() {
    }
}
