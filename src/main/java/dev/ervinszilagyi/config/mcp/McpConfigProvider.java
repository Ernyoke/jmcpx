package dev.ervinszilagyi.config.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import dev.ervinszilagyi.system.ConfigFileLoadingException;
import dev.ervinszilagyi.system.ConfigFileNotFoundException;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Module
public class McpConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(McpConfigProvider.class);

    @Provides
    @Singleton
    public McpConfig provideConfig(final @Named("mcpConfig") File configFile) {
        logger.info("Loading MCP config from {}", configFile);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(configFile, McpConfig.class);
        } catch (final FileNotFoundException fileNotFoundException) {
            logger.error("Failed to load MCP config from {}", configFile, fileNotFoundException);
            throw new ConfigFileNotFoundException(configFile.getAbsoluteFile());
        } catch (final IOException ioException) {
            logger.error("Failed to load MCP config from {}", configFile, ioException);
            throw new ConfigFileLoadingException(ioException, configFile.getAbsoluteFile());
        }
    }
}
