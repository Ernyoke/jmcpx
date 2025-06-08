package dev.ervinszilagyi.commands.session;

import dev.ervinszilagyi.ai.chatmodel.ChatModelWithInfo;
import dev.ervinszilagyi.ai.chatmodel.ModelInfo;
import dev.ervinszilagyi.ai.llmclient.LlmClient;
import dev.ervinszilagyi.ai.mcpserver.McpServerDetailsRetriever;
import dev.ervinszilagyi.ai.memory.SquashedChatMemory;
import dev.ervinszilagyi.md.StylizedPrinter;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.mcp.client.McpPrompt;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.Result;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ChatSession {

    private static final Logger logger = LoggerFactory.getLogger(ChatSession.class);

    private final LlmClient llmClient;
    private final ChatModelWithInfo chatModelWithInfo;
    private final SquashedChatMemory chatMemory;
    private final McpServerDetailsRetriever mcpServerDetailsRetriever;

    private final StylizedPrinter stylizedPrinter;
    private final Terminal terminal;

    private final EnumSet<ChatSessionCommand> commands = EnumSet.allOf(ChatSessionCommand.class);

    @Inject
    public ChatSession(final LlmClient llmClient,
                       final ChatModelWithInfo chatModelWithInfo,
                       final SquashedChatMemory chatMemory,
                       final McpServerDetailsRetriever mcpServerDetailsRetriever,
                       final StylizedPrinter stylizedPrinter,
                       final Terminal terminal) {
        this.llmClient = llmClient;
        this.chatModelWithInfo = chatModelWithInfo;
        this.chatMemory = chatMemory;
        this.mcpServerDetailsRetriever = mcpServerDetailsRetriever;
        this.stylizedPrinter = stylizedPrinter;
        this.terminal = terminal;
    }

    public void openSession() {
        int tokensUsedInCurrentSession = 0;
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        ModelInfo modelInfo = chatModelWithInfo.modelInfo();
        stylizedPrinter.printSystemMessage("Using model " + modelInfo.modelName() + " from " + modelInfo.providerName());
        while (true) {
            try {
                String line = reader.readLine(">> ", null, (MaskingCallback) null, null);
                if (line.isBlank()) {
                    continue;
                }

                Optional<ChatSessionCommand> command = toCommand(line.strip().toLowerCase());

                if (command.isPresent()) {
                    switch (command.orElseThrow()) {
                        case ChatSessionCommand.EXIT:
                            return;
                        case ChatSessionCommand.NEW: {
                            // Clear the memory and notify the user
                            this.chatMemory.clear();
                            this.stylizedPrinter.printSystemMessage("Starting new session. Chat history was cleared!");

                            // Reset token usage
                            tokensUsedInCurrentSession = 0;
                            break;
                        }
                        case ChatSessionCommand.HELP: {
                            stylizedPrinter.printSystemMessage("Commands:");
                            commands.forEach(
                                    cmd -> stylizedPrinter.printSystemMessage(cmd.getDocumentation())
                            );
                            break;
                        }
                        case ChatSessionCommand.TOOLS: {
                            Map<String, List<ToolSpecification>> toolSpecifications =
                                    mcpServerDetailsRetriever.getToolSpecifications(null);
                            for (var entry : toolSpecifications.entrySet()) {
                                stylizedPrinter.printInfoMessage(entry.getKey() + ":\n");
                                for (var tool : entry.getValue()) {
                                    stylizedPrinter.printInfoMessage(" - " + tool.name() + ":\n");
                                    stylizedPrinter.printInfoMessage(tool.description() + "\n");
                                }
                            }
                        }
                        case ChatSessionCommand.PROMPTS: {
                            Map<String, List<McpPrompt>> prompts =
                                    mcpServerDetailsRetriever.getPrompts(null);
                            for (var entry : prompts.entrySet()) {
                                stylizedPrinter.printInfoMessage(entry.getKey() + ":\n");
                                for (var tool : entry.getValue()) {
                                    stylizedPrinter.printInfoMessage(" - " + tool.name() + ":\n");
                                    stylizedPrinter.printInfoMessage(tool.description() + "\n");
                                }
                            }
                        }
                    }
                    continue;
                }

                Result<String> response = llmClient.chat(line, LocalDate.now().toString());
                TokenUsage tokenUsage = response.tokenUsage();
                tokensUsedInCurrentSession += tokenUsage.totalTokenCount();
                stylizedPrinter.printSystemMessage("Response from AI (tokens used: " + tokensUsedInCurrentSession + "):");
                stylizedPrinter.printMarkDown(response.content());

                this.chatMemory.squashToolExecutions();

            } catch (final UserInterruptException | EndOfFileException runtimeException) {
                stylizedPrinter.printSystemMessage("Interrupted by user. Exiting.");
                logger.error(runtimeException.getMessage(), runtimeException);
                break;
            } catch (final RateLimitException rateLimitException) {
                stylizedPrinter.printError("Rate limited by the LLM provider: " + rateLimitException.getMessage());
                logger.error(rateLimitException.getMessage(), rateLimitException);
            } catch (final Exception exception) {
                stylizedPrinter.printError(exception.getMessage());
                logger.error(exception.getMessage(), exception);
            }
        }
    }

    private Optional<ChatSessionCommand> toCommand(final String commandLike) {
        return commands.stream().filter(command -> command.isCommand(commandLike)).findFirst();
    }
}
