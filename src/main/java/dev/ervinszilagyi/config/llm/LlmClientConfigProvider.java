package dev.ervinszilagyi.config.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.IOException;

public class LlmClientConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(LlmClientConfigProvider.class);

    static public LlmConfig loadConfig(final File configFile) throws IOException {
        logger.info("Loading LLM config from {}", configFile);
        TomlParseResult result = Toml.parse(configFile.toPath());

        TomlTable anthropicSection = result.getTableOrEmpty("anthropic");
        AnthropicConfig anthropicConfig = null;
        if (!anthropicSection.isEmpty()) {
            logger.info("Loading Anthropic config.");
            String modelName = anthropicSection.getString("modelName");
            String apiKey = anthropicSection.getString("apiKey");
            boolean isDefault = Boolean.TRUE.equals(anthropicSection.getBoolean("default"));

            if (isDefault) {
                logger.info("Anthropic model {} loaded as default.", modelName);
            } else {
                logger.info("Anthropic model {} loaded.", modelName);
            }

            anthropicConfig = new AnthropicConfig(modelName, apiKey, isDefault);
        }

        TomlTable openAiSection = result.getTableOrEmpty("openai");
        OpenAiConfig openAiConfig = null;
        if (!openAiSection.isEmpty()) {
            logger.info("Loading Anthropic config.");
            String modelName = openAiSection.getString("modelName");
            String apiKey = openAiSection.getString("apiKey");
            boolean isDefault = Boolean.TRUE.equals(openAiSection.getBoolean("default"));

            if (isDefault) {
                logger.info("OpenAI model {} loaded as default.", modelName);
            } else {
                logger.info("OpenAI model {} loaded.", modelName);
            }

            openAiConfig = new OpenAiConfig(modelName, apiKey, isDefault);
        }

        TomlTable googleSection = result.getTableOrEmpty("gemini");
        GoogleConfig googleConfig = null;
        if (!googleSection.isEmpty()) {
            logger.info("Loading Gemini config.");
            String modelName = googleSection.getString("modelName");
            String apiKey = googleSection.getString("apiKey");
            boolean isDefault = Boolean.TRUE.equals(googleSection.getBoolean("default"));

            if (isDefault) {
                logger.info("Google Gemini model {} loaded as default.", modelName);
            } else {
                logger.info("Google Gemini model {} loaded.", modelName);
            }

            googleConfig = new GoogleConfig(modelName, apiKey, isDefault);
        }

        TomlTable bedrockSection = result.getTableOrEmpty("bedrock");
        BedrockConfig bedrockConfig = null;
        if (!bedrockSection.isEmpty()) {
            logger.info("Loading Bedrock config.");
            String modelId = bedrockSection.getString("modelId");
            String modelRegion = bedrockSection.getString("region");
            boolean isDefault = Boolean.TRUE.equals(bedrockSection.getBoolean("default"));

            if (isDefault) {
                logger.info("Bedrock base model {} loaded as default. Region for the model {}", modelId, modelRegion);
            } else {
                logger.info("Bedrock base model {} loaded. Region for the model {}", modelId, modelRegion);
            }

            bedrockConfig = new BedrockConfig(modelId, modelRegion, isDefault);
        }

        return new LlmConfig("name", anthropicConfig, openAiConfig, googleConfig, bedrockConfig);
    }
}
