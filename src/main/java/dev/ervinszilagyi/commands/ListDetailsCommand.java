package dev.ervinszilagyi.commands;

import dev.ervinszilagyi.ai.LlmClientProvider;
import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpConfigProvider;
import dev.ervinszilagyi.mcpserver.McpLogMessageListener;
import dev.ervinszilagyi.md.StylizedPrinter;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@CommandLine.Command(name = "list", description = "List details about available MCP servers.")
public class ListDetailsCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--mcp"}, description = "Location of the mcp.json file.", defaultValue = "mcp.json")
    private File mcpLocation;

    @CommandLine.Option(names = {"-l", "--llm"}, description = "Location of the llm.toml file.", defaultValue = "llm.toml")
    private File llmConfigLocation;

    private static final Logger logger = LoggerFactory.getLogger(ListDetailsCommand.class);

    @Override
    public void run() {
        try {
            Terminal terminal = TerminalBuilder.builder()
                    .jansi(true)
                    .jna(true)
                    .system(true)
                    .build();

            StylizedPrinter stylizedPrinter = new StylizedPrinter(terminal);
            McpLogMessageListener messageListener = new McpLogMessageListener(stylizedPrinter);

            Map<String, McpClient> mcpClients = this.setupMcpClientList(messageListener);

            for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
                try {
                    StringBuilder toolsPart = new StringBuilder();
                    toolsPart.append(entry.getKey()).append("\n");
                    toolsPart.append(" • Tools:").append("\n");
                    for (ToolSpecification tool : entry.getValue().listTools()) {
                        toolsPart.append("  - ")
                                .append(tool.name()).append(": ")
                                .append(tool.description()).append("\n");
                    }
                    stylizedPrinter.printInfoMessage(toolsPart.toString());
                } catch (McpException e) {
                    stylizedPrinter.printSystemMessage("  Tools not supported. System error: " + e.getMessage() + "\n");
                    logger.error(e.getMessage(), e);
                }

                try {
                    StringBuilder resourcesPart = new StringBuilder();
                    resourcesPart.append(" • Resources/Resource Templates:").append("\n");
                    for (McpResource resource : entry.getValue().listResources()) {
                        resourcesPart.append("  - ").append(resource.name()).append(": ")
                                .append(resource.description())
                                .append(" URI: ").append(resource.uri())
                                .append("\n");
                    }
                    for (McpResourceTemplate resourceTemplate : entry.getValue().listResourceTemplates()) {
                        resourcesPart.append("  - ").append(resourceTemplate.name()).append(": ").append(resourceTemplate.uriTemplate())
                                .append("\n");
                    }
                    stylizedPrinter.printInfoMessage(resourcesPart.toString());
                } catch (McpException e) {
                    stylizedPrinter.printSystemMessage("  Resources not supported. System error: " + e.getMessage() + "\n");
                    logger.error(e.getMessage(), e);
                }

                try {
                    StringBuilder promptsPart = new StringBuilder();
                    promptsPart.append(" • Prompts:").append("\n");
                    for (McpPrompt prompt : entry.getValue().listPrompts()) {
                        promptsPart.append("    - ").append(prompt.name()).append(":").append("\n");
                        promptsPart.append("    - ").append(prompt.description()).append(":").append("\n");
                    }
                    stylizedPrinter.printInfoMessage(promptsPart.toString());
                } catch (McpException e) {
                    stylizedPrinter.printSystemMessage("  Prompts not supported. System error: " + e.getMessage() + "\n");
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Map<String, McpClient> setupMcpClientList(McpLogMessageListener mcpLogMessageListener) throws IOException {
        McpConfig mcpConfig = McpConfigProvider.loadConfig(mcpLocation);

        LlmClientProvider llmClientProvider = new LlmClientProvider();

        return llmClientProvider.buildMcpClientList(mcpConfig, mcpLogMessageListener, true);
    }
}
