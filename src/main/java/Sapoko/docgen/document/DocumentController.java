package Sapoko.docgen.document;

import Sapoko.docgen.generation.GeneratedDocx;
import Sapoko.docgen.generation.ProceedGenerationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/documents")
@Validated
public class DocumentController {
    private final DocumentService documentService;
    private static final String DOCX_MEDIATYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Сгенерировать документ")
    @PostMapping
    public ResponseEntity<byte[]> generateDocument(@Valid @RequestBody ProceedGenerationRequest proceedGenerationRequest) {
        GeneratedDocx generatedDocx = documentService.generateDocument(proceedGenerationRequest);
        HttpHeaders httpHeaders = new HttpHeaders();

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                        .filename(generatedDocx.fileName(), StandardCharsets.UTF_8)
                                .build();

        httpHeaders.setContentType(MediaType.parseMediaType(DOCX_MEDIATYPE));
        httpHeaders.setContentDisposition(contentDisposition);

        return ResponseEntity.ok().headers(httpHeaders).body(generatedDocx.docx());
    }

    @Operation(summary = "Получить данные последнего созданного документа по шаблону")
    @ApiResponse(responseCode = "404", description = "Документ не найден")
    @GetMapping("/last")
    public DocumentDto findLastDocument(@RequestParam @Min(1) Long sampleId) {
        return documentService.findLatestDocumentBySampleId(sampleId);
    }
}
