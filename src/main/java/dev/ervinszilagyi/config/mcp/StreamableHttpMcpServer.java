package dev.ervinszilagyi.config.mcp;

import java.util.Map;

public record StreamableHttpMcpServer(Transport transport, String url,
                                      Map<String, String> headers) implements McpServer {
}
