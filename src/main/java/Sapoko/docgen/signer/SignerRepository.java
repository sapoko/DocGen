package Sapoko.docgen.signer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignerRepository extends JpaRepository<Signer, Long> {
    List<Signer> findSignersByActiveTrueOrderByBossGroup();
}
