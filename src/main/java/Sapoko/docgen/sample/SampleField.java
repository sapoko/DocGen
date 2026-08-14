package Sapoko.docgen.sample;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sample_fields")
@Getter
@Setter
public class SampleField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id")
    private Sample sample;

    private String formName;

    @Enumerated(EnumType.STRING)
    private FieldType type;

    @Column(name = "position")
    private int position;

    private String placeholder;
    private boolean active;

    public SampleField() {
    }

    public SampleField(Sample sample, String formName, FieldType type, String placeholder, int position, boolean active) {
        this.sample = sample;
        this.formName = formName;
        this.type = type;
        this.placeholder = placeholder;
        this.position = position;
        this.active = active;
    }
}
