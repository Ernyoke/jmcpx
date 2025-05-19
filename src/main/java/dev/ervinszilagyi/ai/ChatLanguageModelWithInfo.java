package dev.ervinszilagyi.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * Tuple-like record to group a low level {@link ChatLanguageModel} instance and a {@link ModelInfo} instance.
 * @param chatLanguageModel instance of {@link ChatLanguageModel}
 * @param modelInfo information about the instance of {@link ChatLanguageModel}
 */
public record ChatLanguageModelWithInfo(ChatLanguageModel chatLanguageModel, ModelInfo modelInfo) {
}
