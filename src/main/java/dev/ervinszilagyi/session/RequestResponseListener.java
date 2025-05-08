package dev.ervinszilagyi.session;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;

public class RequestResponseListener implements ChatModelListener {
    private final Terminal terminal;
    private final Writer writer;
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseListener.class);

    public RequestResponseListener(final Terminal terminal, final Writer writer) {
        this.terminal = terminal;
        this.writer = writer;
    }

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        ChatRequest chatRequest = requestContext.chatRequest();
        ChatMessage lastMessage = chatRequest.messages().getLast();
        try {
            switch (lastMessage) {
                case UserMessage ignored -> {
                    AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                    attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                            .append("• Contacting LLM with user request.\n");
                    writer.write(attributedStringBuilder.toAnsi(terminal));
                    writer.flush();
                }
                case ToolExecutionResultMessage toolExecutionResultMessage -> {
                    AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                    attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                            .append("• Sending tool ")
                            .append("\"")
                            .append(toolExecutionResultMessage.toolName())
                            .append("\"")
                            .append(" execution result back to LLM.")
                            .append("\n");
                    writer.write(attributedStringBuilder.toAnsi(terminal));
                    writer.flush();
                }
                default -> {
                    AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                    attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                            .append("Error: Unknown message type: ")
                            .append(lastMessage.type().name())
                            .append("\n");
                    writer.write(attributedStringBuilder.toAnsi(terminal));
                    writer.flush();
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
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
                try {
                    writer.write(attributedStringBuilder.toAnsi(terminal));
                    writer.flush();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }
    }
}
