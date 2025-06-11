package dev.ervinszilagyi.app;

import dagger.BindsInstance;
import dagger.Component;
import dev.ervinszilagyi.ai.chatmodel.ChatModelProvider;
import dev.ervinszilagyi.ai.llmclient.LlmClientProvider;
import dev.ervinszilagyi.ai.mcpserver.McpLogMessageHandlerProvider;
import dev.ervinszilagyi.ai.memory.ChatMemoryProvider;
import dev.ervinszilagyi.commands.list.ListMcpDetails;
import dev.ervinszilagyi.commands.session.ChatModelListenerProvider;
import dev.ervinszilagyi.commands.session.ChatSession;
import dev.ervinszilagyi.config.llm.LlmConfigProvider;
import dev.ervinszilagyi.config.mcp.McpConfigProvider;
import dev.ervinszilagyi.terminal.TerminalModule;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jline.terminal.Terminal;

import java.io.File;

/**
 * Dagger component for the application, providing dependencies for the main application.
 * This component is responsible for providing instances of various services and configurations
 * required by the application.
 */
@Component(modules = {
        McpConfigProvider.class,
        LlmConfigProvider.class,
        TerminalModule.class,
        ChatModelProvider.class,
        ChatModelListenerProvider.class,
        ChatMemoryProvider.class,
        LlmClientProvider.class,
        McpLogMessageHandlerProvider.class
})
@Singleton
public interface AppComponent {
    Terminal terminal();

    ChatSession chatSession();

    ListMcpDetails listMcpDetails();

    @Component.Factory
    interface Factory {
        AppComponent create(@BindsInstance @Named("mcpConfig") File mcpConfig,
                            @BindsInstance @Named("llmConfig") File llmConfig,
                            @BindsInstance @Named("detailedLoggingEnabled") boolean isDetailedLoggingEnabled);
    }
}
