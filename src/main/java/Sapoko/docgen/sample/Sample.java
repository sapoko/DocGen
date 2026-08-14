package Sapoko.docgen.sample;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "samples")
@Getter
@Setter
public class Sample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath;
    private String publicName;
    private Short dayOfWeek;
    private boolean hasHospitalTable;

    @Enumerated(EnumType.STRING)
    private Periodicity periodicity;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sample")
    @OrderBy("position asc")
    private List<SampleField> sampleFields = new ArrayList<>();

    public Sample() {
    }

    public Sample(String filePath, String publicName, Short dayOfWeek, boolean hasHospitalTable, Periodicity periodicity) {
        this.filePath = filePath;
        this.publicName = publicName;
        this.dayOfWeek = dayOfWeek;
        this.hasHospitalTable = hasHospitalTable;
        this.periodicity = periodicity;
    }

    public void insertSampleField(SampleField sampleField) {
        sampleFields.add(sampleField);
        sampleField.setSample(this);
    }
}
