package Sapoko.docgen.document;

import java.time.LocalDate;

public record HospitalRowDto(Long id, String fullName, String hospitalTitle, LocalDate admittedAt, int position, String rank, String platoon, String diagnosis) {
}
