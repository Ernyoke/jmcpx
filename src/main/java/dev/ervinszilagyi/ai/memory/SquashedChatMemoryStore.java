package dev.ervinszilagyi.ai.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * In memory store of {@link ChatMessage} messages.
 */
public class SquashedChatMemoryStore implements ChatMemoryStore {
    private List<ChatMessage> messages;
    private final Object memoryId;

    @Inject
    public SquashedChatMemoryStore(@MemoryId String memoryId) {
        this.memoryId = memoryId;
        this.messages = new ArrayList<>();
    }

    @Override
    public List<ChatMessage> getMessages(final Object memoryId) {
        checkMemoryId(memoryId);
        return messages;
    }

    @Override
    public void updateMessages(final Object memoryId, final List<ChatMessage> messages) {
        checkMemoryId(memoryId);
        this.messages = messages;
    }

    @Override
    public void deleteMessages(final Object memoryId) {
        checkMemoryId(memoryId);
        this.messages = new ArrayList<>();
    }

    private void checkMemoryId(final Object memoryId) {
        if (!this.memoryId.equals(memoryId)) {
            throw new IllegalStateException("This chat memory has id: " + this.memoryId +
                    " but an operation has been requested on a memory with id: " + memoryId);
        }
    }
}
