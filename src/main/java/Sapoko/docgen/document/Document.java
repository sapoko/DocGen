package Sapoko.docgen.document;

import Sapoko.docgen.sample.Sample;
import Sapoko.docgen.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id")
    private Sample sample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

    public Document() {
    }

    public Document(Sample sample, User user, LocalDateTime createdAt) {
        this.sample = sample;
        this.user = user;
        this.createdAt = createdAt;
    }
}
