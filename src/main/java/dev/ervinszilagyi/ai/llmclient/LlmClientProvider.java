package dev.ervinszilagyi.ai.llmclient;

import dagger.Module;
import dagger.Provides;
import dev.ervinszilagyi.ai.chatmodel.ChatModelWithInfo;
import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpServer;
import dev.ervinszilagyi.config.mcp.StdioMcpServer;
import dev.ervinszilagyi.config.mcp.StreamableHttpMcpServer;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.logging.McpLogMessageHandler;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

        boolean detailedLoggingEnabled = true;

        Map<String, McpClient> mcpClients = new HashMap<>();
        for (Map.Entry<String, McpServer> entry : mcpConfig.mcpServers().entrySet()) {
            String serverName = entry.getKey();
            McpServer mcpServer = entry.getValue();

            McpTransport transport = switch (mcpServer) {
                case StdioMcpServer stdioMcpServer -> {
                    List<String> command = new ArrayList<>(List.of(stdioMcpServer.command()));
                    command.addAll(stdioMcpServer.args());

                    yield new StdioMcpTransport.Builder()
                            .command(command)
                            .environment(stdioMcpServer.env())
                            .logEvents(detailedLoggingEnabled)
                            .build();
                }
                case StreamableHttpMcpServer streamableHttpMcpServer -> new StreamableHttpMcpTransport.Builder()
                        .url(streamableHttpMcpServer.url())
                        .customHeaders(streamableHttpMcpServer.headers())
                        .build();
                default -> throw new IllegalStateException("Unexpected value: " + mcpServer);
            };

            try {
                mcpClients.put(serverName,
                        new DefaultMcpClient.Builder()
                                .transport(transport)
                                .logHandler(mcpLogMessageHandler)
                                .build());
            } catch (Exception exception) {
                logger.error("Could not start MCP server {}", serverName, exception);
                System.err.println("Could not start MCP server " + serverName);
                switch (exception.getCause()) {
                    case ExecutionException executionException -> {
                        System.err.println("Network connection failed. Reason:" + executionException.getMessage());
                    }
                    case TimeoutException timeoutException -> {
                        System.err.println("Command execution timed out. Reason:" + timeoutException.getMessage());
                    }
                    default -> {
                        Throwable cause = exception.getCause();
                        System.err.println("Reason:" + cause.getMessage());
                    }
                }
            }
        }
        return mcpClients;
    }
}
