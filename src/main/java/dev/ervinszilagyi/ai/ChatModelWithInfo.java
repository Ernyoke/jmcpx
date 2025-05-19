package dev.ervinszilagyi.ai;

import dev.langchain4j.model.chat.ChatModel;

/**
 * Tuple-like record to group a low level {@link ChatModel} instance and a {@link ModelInfo} instance.
 * @param chatModel instance of {@link ChatModel}
 * @param modelInfo information about the instance of {@link ChatModel}
 */
public record ChatModelWithInfo(ChatModel chatModel, ModelInfo modelInfo) {
}
