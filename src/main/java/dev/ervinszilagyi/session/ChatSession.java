package dev.ervinszilagyi.session;

import dev.ervinszilagyi.ai.LlmClient;
import dev.ervinszilagyi.ai.ModelInfo;
import dev.ervinszilagyi.ai.SquashedChatMemory;
import dev.ervinszilagyi.md.StylizedPrinter;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.Result;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

public class ChatSession {

    private static final Logger logger = LoggerFactory.getLogger(ChatSession.class);

    private final LlmClient llmClient;
    private final ModelInfo modelInfo;
    private final SquashedChatMemory chatMemory;

    private final StylizedPrinter stylizedPrinter;

    private final EnumSet<SessionCommand> commands = EnumSet.allOf(SessionCommand.class);

    public ChatSession(final LlmClient llmClient,
                       final ModelInfo modelInfo,
                       final SquashedChatMemory chatMemory,
                       final StylizedPrinter stylizedPrinter) {
        this.llmClient = llmClient;
        this.modelInfo = modelInfo;
        this.chatMemory = chatMemory;
        this.stylizedPrinter = stylizedPrinter;
    }

    public void openSession(Terminal terminal) {
        int tokensUsedInCurrentSession = 0;
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        stylizedPrinter.printSystemMessage("Using model " + modelInfo.modelName() + " from " + modelInfo.providerName());
        while (true) {
            try {
                String line = reader.readLine(">> ", null, (MaskingCallback) null, null);
                if (line.isBlank()) {
                    continue;
                }

                Optional<SessionCommand> command = toCommand(line.strip().toLowerCase());

                if (command.isPresent()) {
                    switch (command.orElseThrow()) {
                        case SessionCommand.EXIT:
                            return;
                        case SessionCommand.NEW: {
                            // Clear the memory and notify the user
                            this.chatMemory.clear();
                            this.stylizedPrinter.printSystemMessage("Starting new session. Chat history was cleared!");

                            // Reset token usage
                            tokensUsedInCurrentSession = 0;
                            break;
                        }
                        case SessionCommand.HELP: {
                            stylizedPrinter.printSystemMessage("Commands:");
                            commands.forEach(
                                    cmd -> stylizedPrinter.printSystemMessage(cmd.getDocumentation())
                            );
                            break;
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

            } catch (UserInterruptException | EndOfFileException e) {
                stylizedPrinter.printSystemMessage("Interrupted by user. Exiting.");
                logger.error(e.getMessage(), e);
                break;
            } catch (RateLimitException rateLimitException) {
                stylizedPrinter.printError("Rate limited by the LLM provider: " + rateLimitException.getMessage());
                logger.error(rateLimitException.getMessage(), rateLimitException);
            } catch (Exception e) {
                stylizedPrinter.printError(e.getMessage());
                logger.error(e.getMessage(), e);
            }
        }
    }

    private Optional<SessionCommand> toCommand(String commandLike) {
        return commands.stream().filter(command -> command.isCommand(commandLike)).findFirst();
    }
}
