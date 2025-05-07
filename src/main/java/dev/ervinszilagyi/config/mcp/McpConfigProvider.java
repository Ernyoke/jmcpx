package dev.ervinszilagyi.config.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class McpConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(McpConfigProvider.class);

    static public McpConfig loadConfig(final File configFile) throws IOException {
        logger.info("Loading MCP config from {}", configFile);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(configFile, McpConfig.class);
    }
}
