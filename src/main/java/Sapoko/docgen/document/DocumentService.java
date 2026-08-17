package Sapoko.docgen.document;

import Sapoko.docgen.sample.FieldType;
import Sapoko.docgen.sample.SampleField;
import Sapoko.docgen.sample.SampleFieldDto;
import Sapoko.docgen.signer.Signer;
import Sapoko.docgen.signer.SignerDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    private static final DateTimeFormatter DOC_FORMAT = DateTimeFormatter.ofPattern("«dd» MMMM yyyy г.", new Locale("ru"));
    private static final DateTimeFormatter HR_DOC_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    private Map<String, String> transformInputs(Document document) {
        List<Input> inputs = document.getInputs();
        Map<String, String> result = new HashMap<>();

        for (Input input : inputs) {
            SampleField sf = input.getSampleField();

            switch (sf.getType()) {
                case DATE -> result.put(sf.getPlaceholder(), LocalDate.parse(input.getValue()).format(DOC_FORMAT));

                case TEXT, NUMBER -> result.put(sf.getPlaceholder(), input.getValue());

                case WEEK_START -> {
                    LocalDate startDate = LocalDate.parse(input.getValue());
                    for (int i = 1; i <= 7; i++) {
                        result.put(sf.getPlaceholder() + i, String.valueOf(startDate.plusDays(i - 1).getDayOfMonth()));
                    }
                }

                case SIGNATORY -> {
                    Signer signer = input.getSigner();
                    result.put(sf.getPlaceholder() + ".post", signer.getPost());
                    result.put(sf.getPlaceholder() + ".rank", signer.getRank());
                    result.put(sf.getPlaceholder() + ".fullName", signer.getFullName());
                }
            }
        }

        return result;
    }

    private List<List<String>> transformHospitalRows(Document document) {
        List<List<String>> result = new ArrayList<>();
        List<HospitalRow> hospitalRows = document.getHospitalRows();

        for (HospitalRow hr : hospitalRows) {
            List<String> row = new ArrayList<>();

            row.add(String.valueOf(hr.getPosition()));
            row.add(hr.getRank());
            row.add(hr.getFullName());
            row.add(hr.getPlatoon());
            row.add(hr.getHospitalTitle());
            row.add(hr.getDiagnosis());
            row.add(hr.getAdmittedAt().format(HR_DOC_FORMAT));

            result.add(row);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public DocumentDto findLatestDocumentBySampleId(long id) {
        Document result = documentRepository.findFirstBySampleIdOrderByCreatedAtDesc(id).orElseThrow(() -> new DocumentNotFoundException(id));
        return toDto(result);
    }

    private DocumentDto toDto(Document document) {
        List<InputDto> inputDtos = new ArrayList<>();
        List<HospitalRowDto> hospitalRowDtos = new ArrayList<>();
        for (Input i : document.getInputs()) {
            SampleField sf = i.getSampleField();
            SampleFieldDto sfDto = new SampleFieldDto(sf.getId(), sf.getFormName(), sf.getType(), sf.getPosition(), sf.getPlaceholder());
            SignerDto signerDto;
            if (sf.getType() == FieldType.SIGNATORY) {
                Signer signer = i.getSigner();
                signerDto = new SignerDto(signer.getId(), signer.getFullName(), signer.getRank(), signer.getPost(), signer.getBossGroup());
            } else {
                signerDto = null;
            }
            inputDtos.add(new InputDto(i.getId(), i.getValue(), sfDto, signerDto));
        }
        for (HospitalRow hr : document.getHospitalRows()) {
            hospitalRowDtos.add(new HospitalRowDto(hr.getId(), hr.getFullName(), hr.getHospitalTitle(), hr.getAdmittedAt(),
                    hr.getPosition(), hr.getRank(), hr.getPlatoon(), hr.getDiagnosis()));
        }
        return new DocumentDto(document.getId(), document.getUser().getUsername(), inputDtos, hospitalRowDtos, document.getCreatedAt());
    }
}
