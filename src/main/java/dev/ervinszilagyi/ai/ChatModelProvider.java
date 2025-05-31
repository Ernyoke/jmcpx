package dev.ervinszilagyi.ai;

import dev.ervinszilagyi.config.llm.*;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.bedrock.BedrockChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

import java.util.List;

public class ChatModelProvider {
    private static final Logger logger = LoggerFactory.getLogger(ChatModelProvider.class);

    /**
     * Builds a {@link ChatModelWithInfo} instance based on the model configuration.
     * Supported models are Anthropic (Claude) models, OpenAI models, Google (Gemini) models and AWS Bedrock models.
     *
     * @param modelConfig         configuration for the chat language model to be created
     * @param isLlmLoggingEnabled if true will enable request and response logging for the model
     * @param listeners           list of {@link ChatModelListener} listener that can be used to catch requests and
     *                            responses to the model
     * @return {@link ChatModel}
     */
    public ChatModelWithInfo buildChatModel(final ModelConfig modelConfig,
                                            final boolean isLlmLoggingEnabled,
                                            final List<ChatModelListener> listeners) {
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
