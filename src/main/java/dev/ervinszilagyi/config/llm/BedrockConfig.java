package dev.ervinszilagyi.config.llm;

public record BedrockConfig(String modelId, String region, boolean isDefault) implements ModelConfig{
    @Override
    public boolean isDefault() {
        return isDefault;
    }
}
