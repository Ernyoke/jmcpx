package dev.ervinszilagyi.commands;

import dev.ervinszilagyi.ai.LlmClient;
import dev.ervinszilagyi.ai.LlmClientProvider;
import dev.ervinszilagyi.ai.SquashedChatMemory;
import dev.ervinszilagyi.ai.SquashedChatMemoryStore;
import dev.ervinszilagyi.config.llm.LlmClientConfigProvider;
import dev.ervinszilagyi.config.llm.LlmConfig;
import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpConfigProvider;
import dev.ervinszilagyi.md.StylizedPrinter;
import dev.ervinszilagyi.session.ChatSession;
import dev.ervinszilagyi.session.RequestResponseListener;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.listener.ChatModelListener;
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

            RequestResponseListener requestResponseListener = new RequestResponseListener(terminal);
            LlmClient llmClient = this.setupLlmClient(chatMemory, requestResponseListener, debugMode);
            StylizedPrinter stylizedPrinter = new StylizedPrinter(terminal);
            ChatSession chatSession = new ChatSession(llmClient, chatMemory, stylizedPrinter);

            chatSession.openSession(terminal);

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private LlmClient setupLlmClient(final ChatMemory chatMemory,
                                     final ChatModelListener chatModelListener,
                                     final boolean isDebugModeEnabled) throws IOException {
        McpConfig mcpConfig = McpConfigProvider.loadConfig(mcpLocation);
        LlmConfig llmConfig = LlmClientConfigProvider.loadConfig(llmConfigLocation);

        LlmClientProvider llmClientProvider = new LlmClientProvider();

        return llmClientProvider.buildLlmClient(mcpConfig,
                llmConfig,
                chatMemory,
                List.of(chatModelListener),
                isDebugModeEnabled);
    }
}
