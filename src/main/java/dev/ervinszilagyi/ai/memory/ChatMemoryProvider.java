package dev.ervinszilagyi.ai.memory;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

@Module
public interface ChatMemoryProvider {
    @Binds
    ChatMemory chatMemory(SquashedChatMemory squashedChatMemory);

    @Binds
    ChatMemoryStore chatMemoryStore(SquashedChatMemoryStore chatMemoryStore);

    @Provides
    @MemoryId
    static String memoryId() {
        return "squashed memory";
    }
}
