package dev.ervinszilagyi.ai;

import dev.ervinszilagyi.config.llm.*;
import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpServer;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.logging.DefaultMcpLogMessageHandler;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.bedrock.BedrockChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LlmClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(LlmClientProvider.class);

    public LlmClient buildLlmClient(final McpConfig mcpConfig,
                                    final LlmConfig llmConfig,
                                    final ChatMemory chatMemory,
                                    final boolean isLlmLoggingEnabled) {
        return buildLlmClient(mcpConfig, llmConfig, chatMemory, List.of(), isLlmLoggingEnabled);
    }

    public LlmClient buildLlmClient(final McpConfig mcpConfig,
                                    final LlmConfig llmConfig,
                                    final ChatMemory chatMemory,
                                    final List<ChatModelListener> listeners,
                                    final boolean isLlmLoggingEnabled) {
        logger.info("Create LlmClient");

        ModelConfig modelConfig = llmConfig.getDefaultConfig();
        ChatLanguageModel model;

        switch (modelConfig) {
            case AnthropicConfig anthropicConfig -> model = AnthropicChatModel.builder()
                    .apiKey(anthropicConfig.apiKey())
                    .modelName(anthropicConfig.modelName())
                    .logRequests(isLlmLoggingEnabled)
                    .logResponses(isLlmLoggingEnabled)
                    .listeners(listeners)
                    .build();
            case OpenAiConfig openAiConfig -> model = OpenAiChatModel.builder()
                    .apiKey(openAiConfig.apiKey())
                    .modelName(openAiConfig.modelName())
                    .logRequests(isLlmLoggingEnabled)
                    .logResponses(isLlmLoggingEnabled)
                    .listeners(listeners)
                    .build();
            case GoogleConfig googleConfig -> model = GoogleAiGeminiChatModel.builder()
                    .modelName(googleConfig.modelName())
                    .apiKey(googleConfig.apiKey())
                    .logRequestsAndResponses(isLlmLoggingEnabled)
                    .listeners(listeners)
                    .build();
            case BedrockConfig bedrockConfig -> model = BedrockChatModel.builder()
                    .modelId(bedrockConfig.modelId())
                    .region(Region.of(bedrockConfig.region()))
                    .logRequests(isLlmLoggingEnabled)
                    .logResponses(isLlmLoggingEnabled)
                    .listeners(listeners)
                    .build();
            default -> throw new IllegalStateException("Unexpected value: " + modelConfig);
        }

        Map<String, McpClient> mcpClients = buildMcpClientList(mcpConfig);
        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClients.values().stream().toList())
                .build();

        return AiServices.builder(LlmClient.class)
                .chatLanguageModel(model)
                .toolProvider(toolProvider)
                .chatMemory(chatMemory)
                .build();
    }

    public Map<String, McpClient> buildMcpClientList(final McpConfig mcpConfig) {
        record McpClientWithName(String name, McpClient mcpClient) {
        }

        return mcpConfig.mcpServers().entrySet().stream()
                .map(entry -> {
                    McpServer mcpServer = entry.getValue();
                    List<String> command = new ArrayList<>(List.of(mcpServer.command()));
                    command.addAll(mcpServer.args());
                    McpTransport transport = new StdioMcpTransport.Builder()
                            .command(command)
                            .environment(mcpServer.env())
                            .logEvents(true)
                            .build();
                    return new McpClientWithName(entry.getKey(), new DefaultMcpClient.Builder()
                            .transport(transport)
                            .logHandler(new DefaultMcpLogMessageHandler())
                            .build());
                })
                .collect(Collectors.toMap(McpClientWithName::name, McpClientWithName::mcpClient));
    }
}
