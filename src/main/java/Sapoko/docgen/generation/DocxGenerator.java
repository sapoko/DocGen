package Sapoko.docgen.generation;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class DocxGenerator implements DocumentGenerator {
    @Override
    public byte[] generate(Map<String, String> placeholders, List<List<String>> tableRows, Path templatePath) throws GenerationException {
        return new byte[0];
    }
}
