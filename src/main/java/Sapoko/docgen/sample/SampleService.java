package Sapoko.docgen.sample;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SampleService {
    private final SampleRepository sampleRepository;

    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @Transactional(readOnly = true)
    public List<SampleDto> getSamplesForDate(LocalDate date) {
        if (date == null) date = LocalDate.now();
        List<Sample> samples = sampleRepository.findSamplesByDayOfWeek((short) date.getDayOfWeek().getValue());

        return samples.stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SampleDto> getSamplesForPeriodicity(Periodicity periodicity) {
        List<Sample> samples = sampleRepository.findSamplesByPeriodicity(periodicity);

        return samples.stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SampleDto getSampleWithFields(long id) {
        Sample result = sampleRepository.findWithFieldsById(id).orElseThrow(() -> new SampleNotFoundException(id));

        return toDto(result);
    }

    private SampleDto toDto(Sample sample) {
        List<SampleFieldDto> sampleFieldsDto = new ArrayList<>();
        for (SampleField s : sample.getSampleFields()) {
            sampleFieldsDto.add(new SampleFieldDto(s.getId(), s.getFormName(), s.getType(), s.getPosition(), s.getPlaceholder()));
        }

        return new SampleDto(sample.getId(), sample.getPublicName(), sample.getDayOfWeek(), sample.isHasHospitalTable(),sample.getPeriodicity(), sampleFieldsDto);
    }
}
