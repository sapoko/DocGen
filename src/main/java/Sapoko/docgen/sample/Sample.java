package Sapoko.docgen.sample;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "samples")
@Getter
@Setter
public class Sample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String filePath;
    private String publicName;
    private Short dayOfWeek;
    private boolean hasHospitalTable;

    @Enumerated(EnumType.STRING)
    private Periodicity periodicity;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sample")
    @OrderBy
    private List<SampleField> sampleFields;

    public Sample() {
    }

    public Sample(String filePath, String publicName, Short dayOfWeek, boolean hasHospitalTable, Periodicity periodicity) {
        this.filePath = filePath;
        this.publicName = publicName;
        this.dayOfWeek = dayOfWeek;
        this.hasHospitalTable = hasHospitalTable;
        this.periodicity = periodicity;
    }
}
