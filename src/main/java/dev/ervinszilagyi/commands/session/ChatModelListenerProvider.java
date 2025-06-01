package dev.ervinszilagyi.commands.session;

import dagger.Binds;
import dagger.Module;
import dev.langchain4j.model.chat.listener.ChatModelListener;

@Module
public interface ChatModelListenerProvider {
    @Binds
    ChatModelListener chatModelListener(RequestResponseListener requestResponseListener);
}
