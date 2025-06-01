package dev.ervinszilagyi.ai.mcpserver;

import dagger.Binds;
import dagger.Module;
import dev.langchain4j.mcp.client.logging.McpLogMessageHandler;

@Module
public interface McpLogMessageHandlerProvider {
    @Binds
    McpLogMessageHandler mcpLogMessageHandler(McpLogMessageListener mcpLogMessageListener);
}
