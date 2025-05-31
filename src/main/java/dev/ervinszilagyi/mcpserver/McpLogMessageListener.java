package dev.ervinszilagyi.mcpserver;

import dev.ervinszilagyi.md.StylizedPrinter;
import dev.langchain4j.mcp.client.logging.McpLogMessage;
import dev.langchain4j.mcp.client.logging.McpLogMessageHandler;

import javax.inject.Inject;

/**
 * Catch system messages from MCP server and log them to the user.
 * Unfortunately, currently it does not work as expected, the handler is not called by LangChain.
 */
public class McpLogMessageListener implements McpLogMessageHandler {
    private final StylizedPrinter stylizedPrinter;

    @Inject
    public McpLogMessageListener(StylizedPrinter stylizedPrinter) {
        this.stylizedPrinter = stylizedPrinter;
    }

    @Override
    public void handleLogMessage(McpLogMessage message) {
        switch (message.level()) {
            case ERROR, CRITICAL, ALERT, EMERGENCY ->
                    stylizedPrinter.printError(message.logger() + " " + message.data());
            default -> stylizedPrinter.printSystemMessage(message.logger() + " " + message.data());
        }
    }
}
