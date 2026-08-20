package Sapoko.docgen.document;

import Sapoko.docgen.generation.*;
import Sapoko.docgen.sample.*;
import Sapoko.docgen.signer.Signer;
import Sapoko.docgen.signer.SignerDto;
import Sapoko.docgen.signer.SignerNotFoundException;
import Sapoko.docgen.signer.SignerRepository;
import Sapoko.docgen.user.User;
import Sapoko.docgen.user.UserNotFoundException;
import Sapoko.docgen.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final SampleRepository sampleRepository;
    private final UserRepository userRepository;
    private final SignerRepository signerRepository;
    private final DocumentGenerator documentGenerator;

    private static final DateTimeFormatter DOC_FORMAT = DateTimeFormatter.ofPattern("«dd» MMMM yyyy г.", new Locale("ru"));
    private static final DateTimeFormatter HR_DOC_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final TemplatesProperties templatesProperties;

    public DocumentService(DocumentRepository documentRepository, SampleRepository sampleRepository, UserRepository userRepository, SignerRepository signerRepository, DocumentGenerator documentGenerator, TemplatesProperties templatesProperties) {
        this.documentRepository = documentRepository;
        this.sampleRepository = sampleRepository;
        this.userRepository = userRepository;
        this.signerRepository = signerRepository;
        this.documentGenerator = documentGenerator;
        this.templatesProperties = templatesProperties;
    }

    @Transactional
    public GeneratedDocx generateDocument(ProceedGenerationRequest proceedGenerationRequest) {
        Sample sample = sampleRepository.findWithFieldsById(proceedGenerationRequest.sampleId())
                                        .orElseThrow(() -> new SampleNotFoundException(proceedGenerationRequest.sampleId()));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                                  .orElseThrow(() -> new UserNotFoundException("Пользователь с именем " + username + " не найден."));
        List<SampleField> sampleFields = sample.getSampleFields();

        Document document = new Document(sample, user, LocalDateTime.now());

        for (ValueFromForm value : proceedGenerationRequest.valueFromForm()) {
            SampleField sampleField = sampleFields.stream()
                .filter(sf -> sf.getId().equals(value.sampleFieldId()))
                .findFirst().orElseThrow(() -> new SampleFieldNotFoundException(value.sampleFieldId()));

            validateUserInput(value, sampleField);

            Input input;
            if (sampleField.getType() != FieldType.SIGNATORY) input = new Input(sampleField, null, value.value());
            else input = new Input(sampleField, signerRepository.findById(value.signerId())
                    .orElseThrow(() -> new SignerNotFoundException(value.signerId())), null);
            document.insertInput(input);
        }

        int counter = 0;
        for (HospitalRowFromForm hr : proceedGenerationRequest.hospitalRowFromForm()) {
            HospitalRow hospitalRow = new HospitalRow(hr.fullName(), hr.hospitalTitle(), hr.admittedAt(),
                                                      ++counter, hr.rank(), hr.platoon(), hr.diagnosis());
            document.insertHospitalRow(hospitalRow);
        }

        documentRepository.saveAndFlush(document);

        String fileName = sample.getPublicName() + " " + document.getCreatedAt().format(HR_DOC_FORMAT) + ".docx";

        return new GeneratedDocx(fileName,
                documentGenerator.generate(transformInputs(document), transformHospitalRows(document),
                Path.of(templatesProperties.templatesPath()).resolve(sample.getFilePath())));
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

    private void validateUserInput(ValueFromForm value, SampleField sampleField) {
        if (sampleField.getType() == FieldType.SIGNATORY) {
            if (StringUtils.hasText(value.value()) || value.signerId() == null) {
                throw new InvalidInputValueException("Поле " + sampleField.getFormName() + " необходимо выбрать из справочника.");
            }
        } else {
            if (!StringUtils.hasText(value.value()) || value.signerId() != null) {
                throw new InvalidInputValueException("Поле " + sampleField.getFormName() + " обязано содержать значение.");
            }
        }
    }
}
