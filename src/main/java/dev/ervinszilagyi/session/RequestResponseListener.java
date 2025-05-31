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

import javax.inject.Inject;
import java.io.PrintWriter;

public class RequestResponseListener implements ChatModelListener {
    private final Terminal terminal;
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseListener.class);

    @Inject
    public RequestResponseListener(final Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        ChatRequest chatRequest = requestContext.chatRequest();
        ChatMessage lastMessage = chatRequest.messages().getLast();
        PrintWriter writer = terminal.writer();
        switch (lastMessage) {
            case UserMessage ignored -> {
                AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT))
                        .append("• Contacting LLM with user request.\n");
                writer.write(attributedStringBuilder.toAnsi(terminal));
                writer.flush();
            }
            case ToolExecutionResultMessage toolExecutionResultMessage -> {
                AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT))
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
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        ChatResponse chatResponse = responseContext.chatResponse();
        AiMessage aiMessage = chatResponse.aiMessage();
        PrintWriter writer = terminal.writer();
        if (aiMessage.hasToolExecutionRequests()) {
            aiMessage.toolExecutionRequests().forEach(toolExecutionRequest -> {
                AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
                attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT))
                        .append("• Calling tool ")
                        .append("\"")
                        .append(toolExecutionRequest.name())
                        .append("\"")
                        .append(" with parameters: ")
                        .append(toolExecutionRequest.arguments())
                        .append("\n")
                        .style(AttributedStyle.DEFAULT);
                writer.write(attributedStringBuilder.toAnsi(terminal));
                writer.flush();
            });
        }
    }
}
