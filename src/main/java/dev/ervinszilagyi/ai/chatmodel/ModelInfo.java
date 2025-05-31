package dev.ervinszilagyi.ai.chatmodel;

/**
 * Store information about the current model in use.
 * @param providerName Name of the LLm provider such as OpenAI, Anthropic, etc.
 * @param modelName Name/ID of the model in use
 */
public record ModelInfo(String providerName, String modelName) {
}
