package dev.ervinszilagyi.ai.mcpserver;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to retrieve information about MCP servers.
 */
@Singleton
public class McpServerDetailsRetriever {
    private final Map<String, McpClient> mcpClients;
    private static final Logger logger = LoggerFactory.getLogger(McpServerDetailsRetriever.class);

    @Inject
    public McpServerDetailsRetriever(final Map<String, McpClient> mcpClients) {
        this.mcpClients = mcpClients;
    }

    /**
     * Return all the available tools from an MCP servers. If the server name is not specified, it will retrieve all the
     * available tools for all MCP servers.
     *
     * @param serverName optional, in case it is null, all the MCP tools for all available servers will be returned
     * @return {@link Map<String, List<ToolSpecification>} map with servername and available tools
     */
    public Map<String, List<ToolSpecification>> getToolSpecifications(final String serverName) {
        Map<String, List<ToolSpecification>> toolsByServer = new HashMap<>();
        if (serverName == null || serverName.isEmpty()) {
            for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
                try {
                    toolsByServer.put(entry.getKey(), entry.getValue().listTools());
                } catch (final McpException mcpException) {
                    logger.error("Could not retrieve tools for server {}. Error {}:",
                            serverName, mcpException.getMessage());
                }
            }
        } else {
            toolsByServer.put(serverName, mcpClients.get(serverName).listTools());
        }
        return toolsByServer;
    }

    /**
     * Return all the available prompts from an MCP server. If the server name is not specified, it will retrieve all the
     * available prompts for all MCP servers.
     *
     * @param serverName optional, in case it is null, all the MCP prompts for all available servers will be returned
     * @return {@link Map<String, List<ToolSpecification>} map with servername and available prompts
     */
    public Map<String, List<McpPrompt>> getPrompts(final String serverName) {
        Map<String, List<McpPrompt>> promptsByServer = new HashMap<>();
        if (serverName == null || serverName.isEmpty()) {
            for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
                try {
                    promptsByServer.put(entry.getKey(), entry.getValue().listPrompts());
                } catch (final McpException mcpException) {
                    logger.error("Could not retrieve prompts for server {}. Error {}:", serverName,
                            mcpException.getMessage());
                }
            }
        } else {
            promptsByServer.put(serverName, mcpClients.get(serverName).listPrompts());
        }
        return promptsByServer;
    }

    /**
     * Return all the available resources from an MCP server. If the server name is not specified, it will retrieve all the
     * available resources for all MCP servers.
     *
     * @param serverName optional, in case it is null, all the MCP resources for all available servers will be returned
     * @return {@link Map<String, List<ToolSpecification>} map with servername and available resources
     */
    public Map<String, List<McpResource>> getResources(final String serverName) {
        Map<String, List<McpResource>> resourcesByServer = new HashMap<>();
        if (serverName == null || serverName.isEmpty()) {
            for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
                try {
                    resourcesByServer.put(entry.getKey(), entry.getValue().listResources());
                } catch (final McpException mcpException) {
                    logger.error("Could not retrieve resources for server {}. Error {}:",
                            serverName, mcpException.getMessage());
                }
            }
        } else {
            resourcesByServer.put(serverName, mcpClients.get(serverName).listResources());
        }
        return resourcesByServer;
    }

    /**
     * Return all the available resource templates from an MCP server. If the server name is not specified, it will retrieve all the
     * available resource templates for all MCP servers.
     *
     * @param serverName optional, in case it is null, all the MCP resource templates for all available servers will be returned
     * @return {@link Map<String, List<ToolSpecification>} map with servername and available resource templates
     */
    public Map<String, List<McpResourceTemplate>> getResourceTemplates(final String serverName) {
        Map<String, List<McpResourceTemplate>> resourceTemplatesByServer = new HashMap<>();
        if (serverName == null || serverName.isEmpty()) {
            for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
                try {
                    resourceTemplatesByServer.put(entry.getKey(), entry.getValue().listResourceTemplates());
                } catch (final McpException mcpException) {
                    logger.error("Could not retrieve resource templates for server {}. Error {}:",
                            serverName, mcpException.getMessage());
                }
            }
        } else {
            resourceTemplatesByServer.put(serverName, mcpClients.get(serverName).listResourceTemplates());
        }
        return resourceTemplatesByServer;
    }
}
