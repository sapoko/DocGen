package Sapoko.docgen.generation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.templates")
public record TemplatesProperties(String templatesPath) {
}
