package dev.ervinszilagyi.config.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Module
public class McpConfigModule {
    private static final Logger logger = LoggerFactory.getLogger(McpConfigModule.class);

    @Provides
    @Singleton
    public McpConfig provideConfig(@Named("mcpConfig") File configFile) {
        logger.info("Loading MCP config from {}", configFile);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(configFile, McpConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
