package dev.ervinszilagyi.config.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
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
        } catch (InvalidTypeIdException invalidTypeIdException) {
            logger.error("Failed to load MCP config from {}", configFile, invalidTypeIdException);
            throw new ConfigFileLoadingException(invalidTypeIdException,
                    "Missing transport type or transport type is wrong for one of the MCP servers. Allowed transport types are http or stdio.",
                    configFile.getAbsoluteFile());
        } catch (final IOException ioException) {
            logger.error("Failed to load MCP config from {}", configFile, ioException);
            throw new ConfigFileLoadingException(ioException, "MCP file not found or it cannot be opened!",
                    configFile.getAbsoluteFile());
        }
    }
}
