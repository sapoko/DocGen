package Sapoko.docgen.sample;

import java.util.List;

public record SampleDto(Long id, String publicName, short dayOfWeek, boolean hasHospitalTable, Periodicity periodicity, List<SampleFieldDto> sampleFields) {
}
