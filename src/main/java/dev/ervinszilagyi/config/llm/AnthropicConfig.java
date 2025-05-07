package dev.ervinszilagyi.config.llm;

public record AnthropicConfig(String modelName, String apiKey, boolean isDefault) implements ModelConfig {
    @Override
    public boolean isDefault() {
        return isDefault;
    }
}
