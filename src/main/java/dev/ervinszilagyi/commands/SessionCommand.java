package dev.ervinszilagyi.commands;

import dev.ervinszilagyi.ai.LlmClient;
import dev.ervinszilagyi.ai.LlmClientProvider;
import dev.ervinszilagyi.config.llm.LlmClientConfigProvider;
import dev.ervinszilagyi.config.llm.LlmConfig;
import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpConfigProvider;
import dev.ervinszilagyi.md.MarkDownPrinter;
import dev.ervinszilagyi.session.ChatSession;
import dev.ervinszilagyi.session.RequestResponseListener;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@CommandLine.Command(name = "session", description = "Start MCP session.")
public class SessionCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--mcp"}, description = "Location of the mcp.json file.", defaultValue = "mcp.json")
    private File mcpLocation;

    @CommandLine.Option(names = {"-l", "--llm"}, description = "Location of the llm.toml file.", defaultValue = "llm.toml")
    private File llmConfigLocation;

    private static final Logger logger = LoggerFactory.getLogger(SessionCommand.class);

    @Override
    public void run() {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(100);

        MarkDownPrinter markDownPrinter = new MarkDownPrinter();

        try {
            Terminal terminal = TerminalBuilder.builder()
                    .jansi(true)
                    .jna(true)
                    .system(true)
                    .build();

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            try (PrintWriter writer = terminal.writer()) {
                RequestResponseListener requestResponseListener = new RequestResponseListener(terminal, writer);
                LlmClient llmClient = this.setupLlmClient(chatMemory, requestResponseListener);
                ChatSession chatSession = new ChatSession(llmClient, chatMemory, markDownPrinter);

                chatSession.openSession(reader, writer);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private LlmClient setupLlmClient(final ChatMemory chatMemory, final ChatModelListener chatModelListener) throws IOException {
        McpConfig mcpConfig = McpConfigProvider.loadConfig(mcpLocation);
        LlmConfig llmConfig = LlmClientConfigProvider.loadConfig(llmConfigLocation);

        LlmClientProvider llmClientProvider = new LlmClientProvider();

        return llmClientProvider.buildLlmClient(mcpConfig,
                llmConfig,
                chatMemory,
                List.of(chatModelListener));
    }
}
