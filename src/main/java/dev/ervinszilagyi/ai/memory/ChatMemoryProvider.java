package dev.ervinszilagyi.ai.memory;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.inject.Singleton;

/**
 * Provides an instance of {@link ChatMemory} and an instance of {@link ChatMemoryStore} for the DI container.
 */
@Module
public interface ChatMemoryProvider {
    @Binds
    ChatMemory chatMemory(final SquashedChatMemory squashedChatMemory);

    @Binds
    ChatMemoryStore chatMemoryStore(final SquashedChatMemoryStore chatMemoryStore);

    @Provides
    @MemoryId
    @Singleton
    static String memoryId() {
        return "squashed memory";
    }
}
