package Sapoko.docgen.document;

import Sapoko.docgen.sample.Sample;
import Sapoko.docgen.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
@Getter
@Setter
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id")
    private Sample sample;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "document", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Input> inputs = new ArrayList<>();

    @OneToMany(mappedBy = "document", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HospitalRow> hospitalRows = new ArrayList<>();

    private LocalDateTime createdAt;

    public Document() {
    }

    public Document(Sample sample, User user, LocalDateTime createdAt) {
        this.sample = sample;
        this.user = user;
        this.createdAt = createdAt;
    }

    public void insertInput(Input input) {
        this.inputs.add(input);
        input.setDocument(this);
    }

    public void insertHospitalRow(HospitalRow hospitalRow) {
        this.hospitalRows.add(hospitalRow);
        hospitalRow.setDocument(this);
    }
}
