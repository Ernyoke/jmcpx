package dev.ervinszilagyi.ai;

import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpServer;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.logging.DefaultMcpLogMessageHandler;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Build a LLM Client based on the configuration provided.
 */
public class LlmClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(LlmClientProvider.class);

    /**
     * Build an {@link LlmClient} based on the configuration provided.
     * @param mcpConfig MCP configuration details
     * @param chatLanguageModel {@link ChatLanguageModel} instance representing the low level LLM client
     * @param chatMemory Chat memory for the LLM client
     * @return {@link LlmClient}
     */
    public LlmClient buildLlmClient(final McpConfig mcpConfig,
                                    final ChatLanguageModel chatLanguageModel,
                                    final ChatMemory chatMemory) {
        logger.info("Create LlmClient");

        Map<String, McpClient> mcpClients = buildMcpClientList(mcpConfig);
        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClients.values().stream().toList())
                .build();

        return AiServices.builder(LlmClient.class)
                .chatLanguageModel(chatLanguageModel)
                .toolProvider(toolProvider)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Build the {@link McpClient} used by the {@link LlmClient} to call MCP tools
     * @param mcpConfig MCP configuration details
     * @return MCP Client
     */
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
