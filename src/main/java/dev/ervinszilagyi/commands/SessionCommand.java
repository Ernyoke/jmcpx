package dev.ervinszilagyi.commands;

import dev.ervinszilagyi.ai.*;
import dev.ervinszilagyi.config.llm.LlmClientConfigProvider;
import dev.ervinszilagyi.config.llm.LlmConfig;
import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpConfigProvider;
import dev.ervinszilagyi.md.StylizedPrinter;
import dev.ervinszilagyi.session.ChatSession;
import dev.ervinszilagyi.session.RequestResponseListener;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.List;

@CommandLine.Command(name = "session", description = "Start MCP session.")
public class SessionCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--mcp"}, description = "Location of the mcp.json file.", defaultValue = "mcp.json")
    private File mcpLocation;

    @CommandLine.Option(names = {"-l", "--llm"}, description = "Location of the llm.toml file.", defaultValue = "llm.toml")
    private File llmConfigLocation;

    @CommandLine.Option(names = {"-d", "--debug"}, description = "Run application is debug mode with. This will enable " +
            "enhanced logging for each request and response to LLMs.")
    private boolean debugMode;

    private static final Logger logger = LoggerFactory.getLogger(SessionCommand.class);

    @Override
    public void run() {
        String storeId = "Squashed Chat Memory";
        SquashedChatMemoryStore squashedChatMemoryStore = new SquashedChatMemoryStore(storeId);
        SquashedChatMemory chatMemory = new SquashedChatMemory(storeId, squashedChatMemoryStore);

        try {
            Terminal terminal = TerminalBuilder.builder()
                    .jansi(true)
                    .jna(true)
                    .system(true)
                    .build();

            McpConfig mcpConfig = McpConfigProvider.loadConfig(mcpLocation);
            LlmConfig llmConfig = LlmClientConfigProvider.loadConfig(llmConfigLocation);

            RequestResponseListener requestResponseListener = new RequestResponseListener(terminal);
            ChatLanguageModelProvider chatLanguageModelProvider = new ChatLanguageModelProvider();

            ChatLanguageModelWithInfo chatLanguageModelWithInfo = chatLanguageModelProvider.buildChatLanguageModel(
                    llmConfig.getDefaultConfig(),
                    debugMode,
                    List.of(requestResponseListener)
            );

            LlmClientProvider llmClientProvider = new LlmClientProvider();
            LlmClient llmClient = llmClientProvider.buildLlmClient(mcpConfig,
                    chatLanguageModelWithInfo.chatLanguageModel(),
                    chatMemory);

            StylizedPrinter stylizedPrinter = new StylizedPrinter(terminal);
            ChatSession chatSession = new ChatSession(llmClient,
                    chatLanguageModelWithInfo.modelInfo(),
                    chatMemory,
                    stylizedPrinter);

            chatSession.openSession(terminal);

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
