package dev.ervinszilagyi.config.llm;

/**
 * Interface for model configurations that require an API key.
 * Implementations should provide the API key used to authenticate requests to the model.
 */
public interface ApiKeyBasedModelConfig {
    String apiKey();
}
