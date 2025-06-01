package dev.ervinszilagyi.ai.mcpserver;

import dagger.Binds;
import dagger.Module;
import dev.langchain4j.mcp.client.logging.McpLogMessageHandler;
import jakarta.inject.Singleton;

@Module
public interface McpLogMessageHandlerProvider {
    @Binds
    @Singleton
    McpLogMessageHandler mcpLogMessageHandler(McpLogMessageListener mcpLogMessageListener);
}
