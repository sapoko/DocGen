package Sapoko.docgen.document;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentDto(Long id, String username, List<InputDto> inputs, List<HospitalRowDto> hospitalRows, LocalDateTime createdAt) {
}
