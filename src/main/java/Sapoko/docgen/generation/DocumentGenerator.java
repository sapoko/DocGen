package Sapoko.docgen.generation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface DocumentGenerator {
    byte[] generate(Map<String, String> placeholders,
                    List<List<String>> tableRows,
                    Path templatePath) throws GenerationException;
}
