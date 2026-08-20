package Sapoko.docgen.signer;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SignerService {
    private final SignerRepository signerRepository;

    public SignerService(SignerRepository signerRepository) {
        this.signerRepository = signerRepository;
    }

    public Map<String, List<SignerDto>> findAllCollected() {
        return signerRepository.findSignersByActiveTrueOrderByBossGroup().stream()
                .map(this::toDto)
                .collect(Collectors.groupingBy(SignerDto::bossGroup, LinkedHashMap::new, Collectors.toList()));
    }

    private SignerDto toDto(Signer signer) {
        return new SignerDto(signer.getId(), signer.getFullName(), signer.getRank(), signer.getPost(), signer.getBossGroup());
    }
}
