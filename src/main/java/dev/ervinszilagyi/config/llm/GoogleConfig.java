package dev.ervinszilagyi.config.llm;

public record GoogleConfig(String modelName, String apiKey, boolean isDefault) implements ModelConfig {
    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String getModelName() {
        return modelName;
    }
}
