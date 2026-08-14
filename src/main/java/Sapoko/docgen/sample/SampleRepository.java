package Sapoko.docgen.sample;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SampleRepository extends JpaRepository<Sample, Long> {
    @EntityGraph(attributePaths = {"sampleFields"})
    List<Sample> findSamplesByDayOfWeek(Short dayOfWeek);

    @EntityGraph(attributePaths = {"sampleFields"})
    List<Sample> findSamplesByPeriodicity(Periodicity periodicity);

    @EntityGraph(attributePaths = {"sampleFields"})
    Optional<Sample> findWithFieldsById(long id);
}
