package dev.ervinszilagyi.session;

import dev.ervinszilagyi.ai.LlmClient;
import dev.ervinszilagyi.md.MarkDownPrinter;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.memory.ChatMemory;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

public class ChatSession {
    private final List<String> commands = List.of(
            "/exit",
            "/new",
            "/clear"
    );

    private static final Logger logger = LoggerFactory.getLogger(ChatSession.class);

    private final LlmClient llmClient;
    private final ChatMemory chatMemory;

    private final MarkDownPrinter markDownPrinter;

    public ChatSession(LlmClient llmClient, ChatMemory chatMemory, MarkDownPrinter markDownPrinter) {
        this.llmClient = llmClient;
        this.chatMemory = chatMemory;
        this.markDownPrinter = markDownPrinter;
    }

    public void openSession(LineReader reader, PrintWriter writer) {
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
                markDownPrinter.print(message, writer);

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

    private boolean isCommand(String userInput) {
        return this.commands.contains(userInput);
    }
}
