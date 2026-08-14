package Sapoko.docgen.document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "hospital_rows")
@Getter
@Setter
public class HospitalRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document
            ;
    private String fullName;
    private String hospitalTitle;
    private LocalDate admittedAt;

    @Column(name = "position")
    private int position;

    private String rank;
    private String platoon;
    private String diagnosis;

    public HospitalRow() {
    }

    public HospitalRow(Document document, String fullName, String hospitalTitle, LocalDate admittedAt, Integer position, String rank, String platoon, String diagnosis) {
        this.document = document;
        this.fullName = fullName;
        this.hospitalTitle = hospitalTitle;
        this.admittedAt = admittedAt;
        this.position = position;
        this.rank = rank;
        this.platoon = platoon;
        this.diagnosis = diagnosis;
    }
}
