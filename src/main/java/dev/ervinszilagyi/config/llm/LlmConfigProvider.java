package dev.ervinszilagyi.config.llm;

import dagger.Module;
import dagger.Provides;
import dev.ervinszilagyi.system.ConfigFileLoadingException;
import dev.ervinszilagyi.system.ConfigFileNotFoundException;
import dev.ervinszilagyi.system.InvalidLlmConfigException;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Module
public class LlmConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(LlmConfigProvider.class);

    /**
     * A functional interface for creating model configurations.
     *
     * @param <T> the type of the model configuration
     */
    @FunctionalInterface
    private interface ConfigCreator<T extends ModelConfig> {
        T create(String modelName, String apiKey, boolean isDefault);
    }

    @Provides
    @Singleton
    public LlmConfig llmConfig(final @Named("llmConfig") File configFile) {
        logger.info("Loading LLM config from {}", configFile);
        TomlParseResult result;
        try {
            result = Toml.parse(configFile.toPath());
        } catch (final NoSuchFileException ioException) {
            logger.error("Failed to load MCP config from {}", configFile, ioException);
            throw new ConfigFileNotFoundException(configFile.getAbsoluteFile());
        } catch (final IOException ioException) {
            logger.error("Failed to load MCP config from {}", configFile, ioException);
            throw new ConfigFileLoadingException(ioException, configFile.getAbsoluteFile());
        }

        LlmConfig llmConfig = new LlmConfig("name", Stream.of(
                        parseApiKeyBasedConfig(result.getArrayOrEmpty("anthropic"),
                                "Anthropic",
                                AnthropicConfig::new),
                        parseApiKeyBasedConfig(result.getArrayOrEmpty("openai"),
                                "OpenAI",
                                OpenAiConfig::new),
                        parseApiKeyBasedConfig(result.getArrayOrEmpty("google"),
                                "Google Gemini",
                                GoogleConfig::new),
                        parseBedrockConfig(result.getArrayOrEmpty("bedrock"))
                ).flatMap(Collection::stream)
                .toList());

        verifyConfig(llmConfig);
        return llmConfig;
    }

    /**
     * Verifies that the provided LLM configuration contains at least one default model configuration.
     *
     * @param llmConfig the LLM configuration to verify
     * @throws InvalidLlmConfigException if no default model configuration is found
     */
    private void verifyConfig(final LlmConfig llmConfig) {
        if (llmConfig.modelConfigs().stream().noneMatch(ModelConfig::isDefault)) {
            throw new InvalidLlmConfigException("LLM configuration must contain at least one default model configuration.");
        }
    }

    /**
     * Parses a TOML array containing API key-based configurations.
     *
     * @param tomlArray     the TOML array to parse
     * @param configName    the name of the configuration (for logging purposes)
     * @param configCreator a function to create the configuration object
     * @param <T>           the type of the configuration object
     * @return a list of parsed configurations
     */
    private static <T extends ModelConfig> List<T> parseApiKeyBasedConfig(final TomlArray tomlArray,
                                                                          final String configName,
                                                                          final ConfigCreator<T> configCreator) {
        List<T> apiKeyBasedConfigs = new ArrayList<>();
        logger.info("Loading {} config.", configName);
        if (tomlArray.isEmpty()) {
            return List.of();
        }

        for (var item : tomlArray.toList()) {
            if (!(item instanceof TomlTable table)) {
                logger.warn("{} section contains non-table item: {}", configName, item);
                continue;
            }
            String modelName = table.getString("modelName");
            String apiKey = table.getString("apiKey");
            boolean isDefault = Boolean.TRUE.equals(table.getBoolean("default"));

            if (isDefault) {
                logger.info("{} model {} loaded as default.", configName, modelName);
            } else {
                logger.info("{} model {} loaded.", configName, modelName);
            }

            apiKeyBasedConfigs.add(configCreator.create(modelName, apiKey, isDefault));
        }

        return apiKeyBasedConfigs;
    }

    private static List<BedrockConfig> parseBedrockConfig(final TomlArray tomlArray) {
        List<BedrockConfig> bedrockConfigs = new ArrayList<>();
        logger.info("Loading Amazon Bedrock config.");
        if (tomlArray.isEmpty()) {
            return List.of();
        }

        for (var item : tomlArray.toList()) {
            if (!(item instanceof TomlTable table)) {
                logger.warn("Amazon Bedrock section contains non-table item: {}", item);
                continue;
            }
            String modelName = table.getString("modelName");
            String apiKey = table.getString("apiKey");
            boolean isDefault = Boolean.TRUE.equals(table.getBoolean("default"));

            if (isDefault) {
                logger.info("Amazon Bedrock model {} loaded as default.", modelName);
            } else {
                logger.info("Amazon Bedrock model {} loaded.", modelName);
            }

            bedrockConfigs.add(new BedrockConfig(modelName, apiKey, isDefault));
        }

        return bedrockConfigs;
    }
}
