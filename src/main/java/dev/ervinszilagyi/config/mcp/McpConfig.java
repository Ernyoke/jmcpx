package dev.ervinszilagyi.config.mcp;

import java.util.Map;

public record McpConfig(Map<String, McpServer> mcpServers) {
}
