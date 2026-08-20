package Sapoko.docgen.signer;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/signers")
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
}
