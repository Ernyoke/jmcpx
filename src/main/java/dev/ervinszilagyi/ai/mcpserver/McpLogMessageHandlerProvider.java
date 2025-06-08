package dev.ervinszilagyi.ai.mcpserver;

import dagger.Binds;
import dagger.Module;
import dev.langchain4j.mcp.client.logging.McpLogMessageHandler;
import jakarta.inject.Singleton;

/**
 * Provides an instance of {@link McpLogMessageHandler} for the MCP client.
 * This handler is responsible for processing log messages from the MCP server.
 */
@Module
public interface McpLogMessageHandlerProvider {
    @Binds
    @Singleton
    McpLogMessageHandler mcpLogMessageHandler(final McpLogMessageListener mcpLogMessageListener);
}
