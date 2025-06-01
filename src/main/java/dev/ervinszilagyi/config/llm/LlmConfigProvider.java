package dev.ervinszilagyi.config.llm;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

@Module
public class LlmConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(LlmConfigProvider.class);

    @Provides
    @Singleton
    public LlmConfig llmConfig(@Named("llmConfig") File configFile) {
        logger.info("Loading LLM config from {}", configFile);
        TomlParseResult result;
        try {
            result = Toml.parse(configFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new LlmConfig("name", Stream.of(
                        parseAnthropicConfig(result.getTableOrEmpty("anthropic")),
                        parseOpenAiConfig(result.getTableOrEmpty("openai")),
                        parseGoogleConfig(result.getTableOrEmpty("gemini")),
                        parseBedrockConfig(result.getTableOrEmpty("bedrock"))
                )
                .filter(Optional::isPresent)
                .flatMap(Optional::stream)
                .toList());
    }

    private static Optional<AnthropicConfig> parseAnthropicConfig(TomlTable anthropicSection) {
        if (anthropicSection.isEmpty()) {
            return Optional.empty();
        }

        logger.info("Loading Anthropic config.");
        String modelName = anthropicSection.getString("modelName");
        String apiKey = anthropicSection.getString("apiKey");
        boolean isDefault = Boolean.TRUE.equals(anthropicSection.getBoolean("default"));

        if (isDefault) {
            logger.info("Anthropic model {} loaded as default.", modelName);
        } else {
            logger.info("Anthropic model {} loaded.", modelName);
        }

        return Optional.of(new AnthropicConfig(modelName, apiKey, isDefault));
    }

    private static Optional<OpenAiConfig> parseOpenAiConfig(TomlTable openAiSection) {
        if (openAiSection.isEmpty()) {
            return Optional.empty();
        }

        logger.info("Loading OpenAI config.");
        String modelName = openAiSection.getString("modelName");
        String apiKey = openAiSection.getString("apiKey");
        boolean isDefault = Boolean.TRUE.equals(openAiSection.getBoolean("default"));

        if (isDefault) {
            logger.info("OpenAI model {} loaded as default.", modelName);
        } else {
            logger.info("OpenAI model {} loaded.", modelName);
        }

        return Optional.of(new OpenAiConfig(modelName, apiKey, isDefault));
    }

    private static Optional<GoogleConfig> parseGoogleConfig(TomlTable googleSection) {
        if (googleSection.isEmpty()) {
            return Optional.empty();
        }

        logger.info("Loading Google Gemini config.");
        String modelName = googleSection.getString("modelName");
        String apiKey = googleSection.getString("apiKey");
        boolean isDefault = Boolean.TRUE.equals(googleSection.getBoolean("default"));

        if (isDefault) {
            logger.info("Google Gemini model {} loaded as default.", modelName);
        } else {
            logger.info("Google Gemini model {} loaded.", modelName);
        }

        return Optional.of(new GoogleConfig(modelName, apiKey, isDefault));
    }

    private static Optional<BedrockConfig> parseBedrockConfig(TomlTable bedrockSection) {
        if (bedrockSection.isEmpty()) {
            return Optional.empty();
        }
        logger.info("Loading Amazon Bedrock config.");
        String modelId = bedrockSection.getString("modelId");
        String modelRegion = bedrockSection.getString("region");
        boolean isDefault = Boolean.TRUE.equals(bedrockSection.getBoolean("default"));

        if (isDefault) {
            logger.info("Bedrock base model {} loaded as default. Region for the model {}", modelId, modelRegion);
        } else {
            logger.info("Bedrock base model {} loaded. Region for the model {}", modelId, modelRegion);
        }

        return Optional.of(new BedrockConfig(modelId, modelRegion, isDefault));
    }
}
