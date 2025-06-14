package dev.ervinszilagyi.ai.llmclient;

import dagger.Module;
import dagger.Provides;
import dev.ervinszilagyi.ai.chatmodel.ChatModelWithInfo;
import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpServer;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.logging.McpLogMessageHandler;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides an {@link LlmClient} instance based on the configuration provided.
 * It also provides a map of MCP clients that can be used to interact with MCP servers.
 */
@Module
public class LlmClientProvider {
    private static final Logger logger = LoggerFactory.getLogger(LlmClientProvider.class);

    /**
     * Build an {@link LlmClient} based on the configuration provided.
     *
     * @param chatModelWithInfo {@link ChatModelWithInfo} instance representing the low level LLM client
     * @param chatMemory        Chat memory for the LLM client
     * @return {@link LlmClient}
     */
    @Provides
    @Singleton
    public LlmClient llmClient(Map<String, McpClient> mcpClients,
                               final ChatModelWithInfo chatModelWithInfo,
                               final ChatMemory chatMemory) {
        logger.info("Create LlmClient");

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClients.values().stream().toList())
                .build();

        return AiServices.builder(LlmClient.class)
                .chatModel(chatModelWithInfo.chatModel())
                .toolProvider(toolProvider)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Build a {@link Map<String, McpClient>} of MCP clients. The map pairs the name of the client with the
     * initialized {@link McpClient} instance.
     *
     * @param mcpConfig            MCP configuration details
     * @param mcpLogMessageHandler Handler form MCP server logs
     * @return {@link Map<String, McpClient>} MCP client list by MCP server name
     */
    @Provides
    public Map<String, McpClient> mcpClients(final McpConfig mcpConfig,
                                             final McpLogMessageHandler mcpLogMessageHandler) {
        record McpClientWithName(String name, McpClient mcpClient) {
        }

        boolean detailedLoggingEnabled = true;

        return mcpConfig.mcpServers().entrySet().stream()
                .map(entry -> {
                    McpServer mcpServer = entry.getValue();
                    List<String> command = new ArrayList<>(List.of(mcpServer.command()));
                    command.addAll(mcpServer.args());
                    McpTransport transport = new StdioMcpTransport.Builder()
                            .command(command)
                            .environment(mcpServer.env())
                            .logEvents(detailedLoggingEnabled)
                            .build();
                    return new McpClientWithName(entry.getKey(), new DefaultMcpClient.Builder()
                            .transport(transport)
                            .logHandler(mcpLogMessageHandler)
                            .build());
                })
                .collect(Collectors.toMap(McpClientWithName::name, McpClientWithName::mcpClient));
    }
}
