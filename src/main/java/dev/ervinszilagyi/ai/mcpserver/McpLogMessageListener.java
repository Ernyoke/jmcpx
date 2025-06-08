package dev.ervinszilagyi.ai.mcpserver;

import dev.ervinszilagyi.md.StylizedPrinter;
import dev.langchain4j.mcp.client.logging.McpLogMessage;
import dev.langchain4j.mcp.client.logging.McpLogMessageHandler;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;

/**
 * Catch system messages from MCP server and log them to the user.
 * Unfortunately, currently it does not work as expected, the handler is not called by LangChain.
 */
@Singleton
public class McpLogMessageListener implements McpLogMessageHandler {
    private final StylizedPrinter stylizedPrinter;

    @Inject
    public McpLogMessageListener(final StylizedPrinter stylizedPrinter) {
        this.stylizedPrinter = stylizedPrinter;
    }

    @Override
    public void handleLogMessage(final McpLogMessage message) {
        switch (message.level()) {
            case ERROR, CRITICAL, ALERT, EMERGENCY ->
                    stylizedPrinter.printError(message.logger() + " " + message.data());
            default -> stylizedPrinter.printSystemMessage(message.logger() + " " + message.data());
        }
    }
}
