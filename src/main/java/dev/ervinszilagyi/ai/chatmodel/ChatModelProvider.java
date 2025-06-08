package dev.ervinszilagyi.ai.chatmodel;

import dagger.Module;
import dagger.Provides;
import dev.ervinszilagyi.config.llm.*;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.bedrock.BedrockChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.inject.Singleton;
import software.amazon.awssdk.regions.Region;

import java.util.List;

/**
 * Provides an instance of {@link ChatModel} and a {@link ModelInfo} for the DI container.
 * The model is created based on the configuration provided in {@link LlmConfig}.
 */
@Module
public class ChatModelProvider {
    @Provides
    @Singleton
    public ChatModelWithInfo chatModel(final LlmConfig llmConfig,
                                       final ChatModelListener chatModelListener) {
        ModelConfig modelConfig = llmConfig.getDefaultConfig();
        List<ChatModelListener> listeners = List.of(chatModelListener);
        boolean isLlmLoggingEnabled = true;
        ChatModel chatModel = switch (modelConfig) {
            case AnthropicConfig anthropicConfig -> AnthropicChatModel.builder()
                    .apiKey(anthropicConfig.apiKey())
                    .modelName(anthropicConfig.modelName())
                    .logRequests(isLlmLoggingEnabled)
                    .logResponses(isLlmLoggingEnabled)
                    .listeners(listeners)
                    .build();
            case OpenAiConfig openAiConfig -> OpenAiChatModel.builder()
                    .apiKey(openAiConfig.apiKey())
                    .modelName(openAiConfig.modelName())
                    .logRequests(isLlmLoggingEnabled)
                    .logResponses(isLlmLoggingEnabled)
                    .listeners(listeners)
                    .build();
            case GoogleConfig googleConfig -> GoogleAiGeminiChatModel.builder()
                    .modelName(googleConfig.modelName())
                    .apiKey(googleConfig.apiKey())
                    .logRequestsAndResponses(isLlmLoggingEnabled)
                    .listeners(listeners)
                    .build();
            case BedrockConfig bedrockConfig -> BedrockChatModel.builder()
                    .modelId(bedrockConfig.modelId())
                    .region(Region.of(bedrockConfig.region()))
                    .logRequests(isLlmLoggingEnabled)
                    .logResponses(isLlmLoggingEnabled)
                    .listeners(listeners)
                    .build();
            default -> throw new IllegalStateException("Unexpected value: " + modelConfig);
        };

        return new ChatModelWithInfo(chatModel,
                buildModelInfo(chatModel.provider(), modelConfig));
    }

    /**
     * Returns the {@link ModelInfo} storing a nice name of the provider and the model name/ID.
     *
     * @param modelProvider Enum representing the list of available providers
     * @param modelConfig   model config object from which the name/ID of the model is retrieved
     * @return {@link ModelInfo}
     */
    private ModelInfo buildModelInfo(ModelProvider modelProvider, ModelConfig modelConfig) {
        String providerName = switch (modelProvider) {
            case ANTHROPIC -> "Anthropic";
            case OPEN_AI -> "OpenAI";
            case AMAZON_BEDROCK -> "Amazon Bedrock";
            case GOOGLE_AI_GEMINI -> "Google Ai Gemini";
            default -> throw new IllegalStateException("Unexpected value: " + modelConfig);
        };

        return new ModelInfo(providerName, modelConfig.getModelName());
    }
}
