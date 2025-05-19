package dev.ervinszilagyi.config.llm;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record LlmConfig(String name,
                        AnthropicConfig anthropicConfig,
                        OpenAiConfig openAiConfig,
                        GoogleConfig googleConfig,
                        BedrockConfig bedrockConfig) {
    public ModelConfig getDefaultConfig() {
        // To allow null values here we should avoid using List.of
        List<ModelConfig> modelConfigs = Arrays.asList(anthropicConfig,
                openAiConfig, googleConfig, bedrockConfig);
        return modelConfigs.stream().filter(Objects::nonNull).filter(ModelConfig::isDefault).findFirst().orElseThrow();
    }
}
