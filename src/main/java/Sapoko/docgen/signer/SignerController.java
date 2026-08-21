package Sapoko.docgen.signer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/signers")
@Validated
public class SignerController {
    private final SignerService signerService;

    public SignerController(SignerService signerService) {
        this.signerService = signerService;
    }

    @Operation(summary = "Получить всех подписантов сгруппированных по группам командования")
    @GetMapping
    public Map<String, List<SignerDto>> findSignersGroupedByBossgroup() {
        return signerService.findAllCollected();
    }

    @Operation(summary = "Добавить нового подписанта")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SignerDto addSigner(@Valid @RequestBody SignerRequest signerRequest) {
        return signerService.createSigner(signerRequest);
    }

    @Operation(summary = "Обновить поля подписанта")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public SignerDto updateSigner(@Valid @RequestBody SignerRequest signerRequest,
                                  @PathVariable @Min(1) Long id) {
        return signerService.updateSigner(id, signerRequest);
    }

    @Operation(summary = "Сделать подписанта неактивным")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateSigner(@PathVariable @Min(1) Long id) {
        signerService.deactivateSigner(id);
    }

    @Operation(summary = "Получить все значения групп подписантов")
    @GetMapping("/groups")
    public List<BossGroupDto> getAllBossGroups() {
        return signerService.getAllBossGroups();
    }
}
