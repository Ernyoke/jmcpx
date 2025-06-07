package dev.ervinszilagyi.config.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.SystemException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Module
public class McpConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(McpConfigProvider.class);

    @Provides
    @Singleton
    public McpConfig provideConfig(@Named("mcpConfig") File configFile) {
        logger.info("Loading MCP config from {}", configFile);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(configFile, McpConfig.class);
        } catch (FileNotFoundException fileNotFoundException) {
            logger.error("Failed to load MCP config from {}", configFile, fileNotFoundException);
            throw new SystemException(SystemException.ErrorType.MCP_CONFIG_NOT_FOUND,
                    "MCP config file '" + configFile.getAbsolutePath() + "' not found!");
        } catch (IOException ioException) {
            logger.error("Failed to load MCP config from {}", configFile, ioException);
            throw new SystemException(SystemException.ErrorType.MCP_CONFIG_COULD_NOT_BE_LOADED,
                    ioException.getMessage());
        }
    }
}
