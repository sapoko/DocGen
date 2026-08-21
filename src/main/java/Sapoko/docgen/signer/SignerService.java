package Sapoko.docgen.signer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SignerService {
    private final SignerRepository signerRepository;

    public SignerService(SignerRepository signerRepository) {
        this.signerRepository = signerRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, List<SignerDto>> findAllCollected() {
        return signerRepository.findSignersByActiveTrueOrderByBossGroup().stream()
                .map(this::toDto)
                .collect(Collectors.groupingBy(s -> s.bossGroup().getDisplayName(), LinkedHashMap::new, Collectors.toList()));
    }

    @Transactional
    public SignerDto createSigner(SignerRequest signerRequest) {
        Signer signer = new Signer(signerRequest.rankShort(), signerRequest.post(), true, signerRequest.bossGroup(),
                signerRequest.rankGen(), signerRequest.lastNameGen(), signerRequest.givenNamesGen(), signerRequest.rankNom(),
                signerRequest.lastNameNom(), signerRequest.givenNamesNom(), signerRequest.rankIns(), signerRequest.lastNameIns(),
                signerRequest.givenNamesIns());

        signer = signerRepository.save(signer);

        return toDto(signer);
    }

    @Transactional
    public SignerDto updateSigner(Long id,
                                  SignerRequest signerRequest) {
        Signer signer = signerRepository.findById(id).orElseThrow(() -> new SignerNotFoundException(id));

        signer.setBossGroup(signerRequest.bossGroup());
        signer.setGivenNamesGen(signerRequest.givenNamesGen());
        signer.setPost(signerRequest.post());
        signer.setGivenNamesIns(signerRequest.givenNamesIns());
        signer.setGivenNamesNom(signerRequest.givenNamesNom());
        signer.setLastNameGen(signerRequest.lastNameGen());
        signer.setLastNameNom(signerRequest.lastNameNom());
        signer.setLastNameIns(signerRequest.lastNameIns());
        signer.setRankGen(signerRequest.rankGen());
        signer.setRankIns(signerRequest.rankIns());
        signer.setRankNom(signerRequest.rankNom());
        signer.setRankShort(signerRequest.rankShort());

        return toDto(signer);
    }

    @Transactional
    public void deactivateSigner(Long id) {
        Signer signer = signerRepository.findById(id).orElseThrow(() -> new SignerNotFoundException(id));

        signer.setActive(false);
    }

    public List<BossGroupDto> getAllBossGroups() {
        return Arrays.stream(BossGroup.values())
                .map(s -> new BossGroupDto(s.name(), s.getDisplayName()))
                .toList();
    }

    public SignerDto toDto(Signer signer) {
        return new SignerDto(
                signer.getId(),
                signer.getRankShort(),
                signer.getPost(),
                signer.isActive(),
                signer.getBossGroup(),
                signer.getRankGen(),
                signer.getLastNameGen(),
                signer.getGivenNamesGen(),

                signer.getRankNom(),
                signer.getLastNameNom(),
                signer.getGivenNamesNom(),

                signer.getRankIns(),
                signer.getLastNameIns(),
                signer.getGivenNamesIns());
    }
}
