package Sapoko.docgen.document;

import Sapoko.docgen.sample.SampleField;
import Sapoko.docgen.signer.Signer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inputs")
@Getter
@Setter
public class Input {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_field_id")
    private SampleField sampleField;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signer_id")
    private Signer signer;

    private String value;

    public Input() {
    }

    public Input(Document document, SampleField sampleField, Signer signer, String value) {
        this.document = document;
        this.sampleField = sampleField;
        this.signer = signer;
        this.value = value;
    }
}
