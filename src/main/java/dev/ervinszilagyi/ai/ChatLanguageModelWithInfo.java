package dev.ervinszilagyi.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;

public record ChatLanguageModelWithInfo(ChatLanguageModel chatLanguageModel, ModelInfo modelInfo) {
}
