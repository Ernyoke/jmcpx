package dev.ervinszilagyi.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SquashedChatMemory implements ChatMemory {
    private static final Logger log = LoggerFactory.getLogger(SquashedChatMemory.class);

    private final Object id;
    private final ChatMemoryStore store;

    public SquashedChatMemory(Object id) {
        this.id = id;
        this.store = new SquashedChatMemoryStore(id);
    }

    @Override
    public Object id() {
        return this.id;
    }

    @Override
    public void add(ChatMessage message) {
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

    public void squashToolExecutions() {
        List<ChatMessage> messages = store.getMessages(id);
        List<ChatMessage> squashedMessages = new ArrayList<>();
        for (ChatMessage message : messages) {
            if (message instanceof AiMessage aiMessage) {
                if (aiMessage.hasToolExecutionRequests()) {
                    continue;
                }
            }
            if (message instanceof ToolExecutionResultMessage) {
                continue;
            }
            squashedMessages.add(message);
        }
        this.store.updateMessages(id, squashedMessages);
    }

    private static Optional<SystemMessage> findSystemMessage(List<ChatMessage> messages) {
        return messages.stream()
                .filter(message -> message instanceof SystemMessage)
                .map(message -> (SystemMessage) message)
                .findAny();
    }
}

