package Sapoko.docgen.sample;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/samples")
@Validated
public class SampleController {
    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @Operation(summary = "Получить шаблоны по дате")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации даты")
    @GetMapping
    public List<SampleDto> findSamplesByDateOrPeriodicity(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return sampleService.getSamplesForDate(date);
    }

    @Operation(summary = "Получить ежемесячные шаблоны")
    @GetMapping("/monthly")
    public List<SampleDto> findMonthlySamples() {
        return sampleService.getSamplesForPeriodicity(Periodicity.MONTHLY);
    }

    @Operation(summary = "Найти шаблон по id")
    @ApiResponse(responseCode = "404", description = "Шаблон не найден")
    @GetMapping("/{id}")
    public SampleDto findSampleById(@PathVariable Long id) {
        return sampleService.getSampleWithFields(id);
    }
}
