package dev.ervinszilagyi.config.llm;

import java.util.List;
import java.util.Objects;

public record LlmConfig(String name,
                        AnthropicConfig anthropicConfig,
                        OpenAiConfig openAiConfig,
                        BedrockConfig bedrockConfig) {
    public ModelConfig getDefaultConfig() {
        List<ModelConfig> modelConfigs = List.of(anthropicConfig, openAiConfig, bedrockConfig);
        return modelConfigs.stream().filter(Objects::nonNull).filter(ModelConfig::isDefault).findFirst().orElseThrow();
    }
}
