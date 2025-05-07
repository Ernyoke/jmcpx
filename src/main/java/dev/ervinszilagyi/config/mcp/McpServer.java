package dev.ervinszilagyi.config.mcp;

import java.util.List;
import java.util.Map;

public record McpServer(String command, List<String> args, Map<String, String> env) {
}
