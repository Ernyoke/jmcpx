package dev.ervinszilagyi.commands;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import dev.ervinszilagyi.ai.LlmClient;
import dev.ervinszilagyi.ai.LlmClientProvider;
import dev.ervinszilagyi.config.llm.LlmClientConfigProvider;
import dev.ervinszilagyi.config.llm.LlmConfig;
import dev.ervinszilagyi.config.mcp.McpConfig;
import dev.ervinszilagyi.config.mcp.McpConfigProvider;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@CommandLine.Command(name = "session", description = "Start MCP session.")
public class SessionCommand implements Runnable {
    @CommandLine.ParentCommand
    private InitCommand parent;

    @CommandLine.Option(names = {"-c", "--mcp"}, description = "Location of the mcp.json file.", defaultValue = "mcp.json")
    private File mcpLocation;

    @CommandLine.Option(names = {"-l", "--llm"}, description = "Location of the llm.toml file.", defaultValue = "llm.toml")
    private File llmConfigLocation;

    private static final Logger logger = LoggerFactory.getLogger(SessionCommand.class);

    private final ChatMemory chatMemory;

    private final List<String> commands = List.of(
            "/exit",
            "/new",
            "/clear"
    );

    private final Parser parser;
    private final Formatter formatter;

    public SessionCommand() {
        chatMemory = MessageWindowChatMemory.withMaxMessages(100);

        DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
                Extensions.ALL
        );

        MutableDataSet FORMAT_OPTIONS = new MutableDataSet();
        FORMAT_OPTIONS.set(Parser.EXTENSIONS, Parser.EXTENSIONS.get(OPTIONS));

        parser = Parser.builder(OPTIONS).build();
        formatter = Formatter.builder(FORMAT_OPTIONS).build();
    }

    @Override
    public void run() {
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

                ChatModelListener requestResponseLogger = new ChatModelListener() {
                    @Override
                    public void onRequest(ChatModelRequestContext requestContext) {
                        ChatRequest chatRequest = requestContext.chatRequest();
                        ChatMessage lastMessage = chatRequest.messages().getLast();

                        switch (lastMessage) {
                            case UserMessage ignored -> {
                                AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                                attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                                        .append("• Contacting LLM with user request.\n")
                                        .style(AttributedStyle.DEFAULT);
                                writer.write(attributedStringBuilder.toAnsi(terminal));
                            }
                            case ToolExecutionResultMessage toolExecutionResultMessage -> {
                                AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                                attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                                        .append("• Sending tool ")
                                        .append("\"")
                                        .append(toolExecutionResultMessage.toolName())
                                        .append("\"")
                                        .append(" execution result back to LLM.")
                                        .append("\n")
                                        .style(AttributedStyle.DEFAULT);
                                writer.write(attributedStringBuilder.toAnsi(terminal));
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + lastMessage);
                        }
                        writer.flush();
                    }

                    @Override
                    public void onResponse(ChatModelResponseContext responseContext) {
                        ChatResponse chatResponse = responseContext.chatResponse();
                        AiMessage aiMessage = chatResponse.aiMessage();
                        if (aiMessage.hasToolExecutionRequests()) {
                            aiMessage.toolExecutionRequests().forEach(toolExecutionRequest -> {
                                AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                                attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                                        .append("• Calling tool ")
                                        .append("\"")
                                        .append(toolExecutionRequest.name())
                                        .append("\"")
                                        .append(" with parameters: ")
                                        .append(toolExecutionRequest.arguments())
                                        .append("\n")
                                        .style(AttributedStyle.DEFAULT);
                                writer.write(attributedStringBuilder.toAnsi(terminal));
                            });
                        }
                    }
                };

                LlmClient llmClient = this.setupLlmClient(requestResponseLogger);

                while (true) {
                    try {
                        String line = reader.readLine(">> ", null, (MaskingCallback) null, null);
                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        if (isCommand(line)) {
                            switch (line) {
                                case "/exit":
                                    return;
                                case "/clear": {
                                    chatMemory.clear();
                                    break;
                                }
                                default:
                                    throw new Exception("Unknown command: " + line);
                            }
                            continue;
                        }

                        String message = llmClient.chat(line, LocalDate.now().toString());
                        printMdContent(message, writer);

                    } catch (UserInterruptException | EndOfFileException e) {
                        writer.println("Interrupted by user. Exiting.");
                        logger.error(e.getMessage(), e);
                        break;
                    } catch (RateLimitException rateLimitException) {
                        writer.println("Rate Limit ERROR: " + rateLimitException.getMessage());
                        logger.error(rateLimitException.getMessage(), rateLimitException);
                    } catch (Exception e) {
                        writer.println("ERROR: " + e.getMessage());
                        logger.error(e.getMessage(), e);
                    } finally {
                        writer.flush();
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private LlmClient setupLlmClient(ChatModelListener chatModelListener) throws IOException {
        McpConfig mcpConfig = McpConfigProvider.loadConfig(mcpLocation);
        LlmConfig llmConfig = LlmClientConfigProvider.loadConfig(llmConfigLocation);

        LlmClientProvider llmClientProvider = new LlmClientProvider();

        return llmClientProvider.buildLlmClient(mcpConfig,
                llmConfig,
                this.chatMemory,
                List.of(chatModelListener));
    }

    private boolean isCommand(String userInput) {
        return this.commands.contains(userInput);
    }

    private void printMdContent(String content, PrintWriter writer) {
        Node document = parser.parse(content);
        String commonmark = formatter.render(document);
        writer.println(commonmark);
    }
}
