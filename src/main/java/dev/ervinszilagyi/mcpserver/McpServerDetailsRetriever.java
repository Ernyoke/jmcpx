package dev.ervinszilagyi.mcpserver;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class McpServerDetailsRetriever {
    private final Map<String, McpClient> mcpClients;
    private static final Logger logger = LoggerFactory.getLogger(McpServerDetailsRetriever.class);

    @Inject
    public McpServerDetailsRetriever(Map<String, McpClient> mcpClients) {
        this.mcpClients = mcpClients;
    }

    public Map<String, List<ToolSpecification>> getToolSpecifications(final String serverName) {
        Map<String, List<ToolSpecification>> toolsByServer = new HashMap<>();
        if (serverName == null || serverName.isEmpty()) {
            for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
                try {
                    toolsByServer.put(entry.getKey(), entry.getValue().listTools());
                } catch (McpException e) {
                    logger.error("Could not retrieve tools for server {}. Error {}:", serverName, e.getMessage());
                }
            }
        }
        return toolsByServer;
    }
}
