package dev.ervinszilagyi.config.llm;

import java.util.List;
import java.util.Objects;

public record LlmConfig(String name, List<? extends ModelConfig> modelConfigs) {
    public ModelConfig getDefaultConfig() {
        return modelConfigs.stream()
                .filter(Objects::nonNull).filter(ModelConfig::isDefault).findFirst().orElseThrow();
    }
}
