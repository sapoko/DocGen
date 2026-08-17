package Sapoko.docgen.document;

import Sapoko.docgen.sample.SampleFieldDto;
import Sapoko.docgen.signer.SignerDto;

public record InputDto(Long id, String value, SampleFieldDto sampleField, SignerDto signerDto) {
}
