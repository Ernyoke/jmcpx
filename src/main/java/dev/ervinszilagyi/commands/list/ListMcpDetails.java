package dev.ervinszilagyi.commands.list;

import dev.ervinszilagyi.ai.mcpserver.McpServerDetailsRetriever;
import dev.ervinszilagyi.md.StylizedPrinter;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpPrompt;
import dev.langchain4j.mcp.client.McpResource;
import dev.langchain4j.mcp.client.McpResourceTemplate;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class ListMcpDetails {
    private static final Logger logger = LoggerFactory.getLogger(ListMcpDetails.class);

    private final McpServerDetailsRetriever mcpServerDetailsRetriever;
    private final StylizedPrinter stylizedPrinter;

    @Inject
    public ListMcpDetails(final McpServerDetailsRetriever mcpServerDetailsRetriever, final StylizedPrinter stylizedPrinter) {
        this.mcpServerDetailsRetriever = mcpServerDetailsRetriever;
        this.stylizedPrinter = stylizedPrinter;
    }

    public void displayDetails(String serverName) {
        Map<String, List<ToolSpecification>> toolsByServer = mcpServerDetailsRetriever.getToolSpecifications(serverName);
        Map<String, List<McpPrompt>> promptsByServer = mcpServerDetailsRetriever.getPrompts(serverName);
        Map<String, List<McpResource>> resourcesByServer = mcpServerDetailsRetriever.getResources(serverName);
        Map<String, List<McpResourceTemplate>> resourceTemplatesByServer = mcpServerDetailsRetriever.getResourceTemplates(serverName);

        Set<String> servers = new HashSet<>();
        servers.addAll(toolsByServer.keySet());
        servers.addAll(promptsByServer.keySet());
        servers.addAll(resourcesByServer.keySet());
        servers.addAll(resourceTemplatesByServer.keySet());

        StringBuilder stringBuilder = new StringBuilder();
        for (String server : servers) {
            stringBuilder.append(server).append("\n");
            if (toolsByServer.containsKey(server)) {
                stringBuilder.append(" • Tools:").append("\n");
                for (ToolSpecification tool : toolsByServer.get(server)) {
                    stringBuilder.append("  - ")
                            .append(tool.name()).append(": ")
                            .append(tool.description()).append("\n");
                }
                stringBuilder.append("\n");
            }

            if (promptsByServer.containsKey(server)) {
                stringBuilder.append(" • Prompts:").append("\n");
                for (McpPrompt tool : promptsByServer.get(server)) {
                    stringBuilder.append("  - ")
                            .append(tool.name()).append(": ")
                            .append(tool.description()).append("\n");
                }
                stringBuilder.append("\n");
            }

            if (resourcesByServer.containsKey(server)) {
                stringBuilder.append(" • Resources:").append("\n");
                for (McpResource resource : resourcesByServer.get(server)) {
                    stringBuilder.append("  - ").append(resource.name()).append(": ")
                            .append(resource.description())
                            .append(" URI: ").append(resource.uri())
                            .append("\n");
                }
                stringBuilder.append("\n");
            }

            if (resourceTemplatesByServer.containsKey(server)) {
                stringBuilder.append(" • Resource Templates:").append("\n");
                for (McpResourceTemplate resourceTemplate : resourceTemplatesByServer.get(server)) {
                    stringBuilder.append("  - ")
                            .append(resourceTemplate.name()).append(": ")
                            .append(resourceTemplate.uriTemplate())
                            .append("\n");
                }
                stringBuilder.append("\n");
            }
        }
        stylizedPrinter.printInfoMessage(stringBuilder.toString());
    }
}
