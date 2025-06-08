package dev.ervinszilagyi.ai.memory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * /**
 * Represents a store for the {@link ChatMemory} state with the addition that the chain of tool request/response
 * messages can be squashed.
 */
public class SquashedChatMemory implements ChatMemory {
    private static final Logger logger = LoggerFactory.getLogger(SquashedChatMemory.class);

    private final Object id;
    private final ChatMemoryStore store;

    @Inject
    public SquashedChatMemory(final @MemoryId String id, final SquashedChatMemoryStore store) {
        this.id = id;
        this.store = store;
    }

    @Override
    public Object id() {
        return this.id;
    }

    @Override
    public void add(final ChatMessage message) {
        List<ChatMessage> messages = messages();
        if (message instanceof SystemMessage) {
            Optional<SystemMessage> systemMessage = findSystemMessage(messages);
            if (systemMessage.isPresent()) {
                if (systemMessage.get().equals(message)) {
                    return; // do not add the same system message
                } else {
                    messages.remove(systemMessage.get()); // need to replace existing system message
                }
            }
        }
        messages.add(message);
        store.updateMessages(id, messages);
    }

    @Override
    public List<ChatMessage> messages() {
        return new LinkedList<>(store.getMessages(id));
    }

    @Override
    public void clear() {
        this.store.deleteMessages(this.id);
    }

    /**
     * Squash tool execution messages by removing AI messages with tool execution requests and ToolExecutionResultMessage
     * messages. After every ToolExecutionResultMessage there is an AIMessage that will either request other tool call
     * or it will summarize the result. We can keep only the final AIMessage that represents the final answer of the LLM.
     * <p>
     * Warning: this method should be called after a chain of Tool calls has ended and the LLM formalized a final
     * response for the user query.
     */
    public void squashToolExecutions() {
        List<ChatMessage> messages = store.getMessages(id);
        List<ChatMessage> squashedMessages = messages.stream()
                .filter(message -> switch (message) {
                    case AiMessage aiMessage -> !aiMessage.hasToolExecutionRequests();
                    case ToolExecutionResultMessage ignored -> false;
                    default -> true;
                }).toList();
        if (messages.size() != squashedMessages.size()) {
            logger.info("Chat memory squashed. Number of messages evicted: {}", messages.size() - squashedMessages.size());
            this.store.updateMessages(id, squashedMessages);
        }
    }

    private static Optional<SystemMessage> findSystemMessage(final List<ChatMessage> messages) {
        return messages.stream()
                .filter(message -> message instanceof SystemMessage)
                .map(message -> (SystemMessage) message)
                .findAny();
    }
}

