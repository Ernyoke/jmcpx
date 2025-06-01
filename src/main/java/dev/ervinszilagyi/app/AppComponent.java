package dev.ervinszilagyi.app;

import dagger.BindsInstance;
import dagger.Component;
import dev.ervinszilagyi.ai.chatmodel.ChatModelProvider;
import dev.ervinszilagyi.ai.chatmodel.ChatModelWithInfo;
import dev.ervinszilagyi.ai.llmclient.LlmClient;
import dev.ervinszilagyi.ai.llmclient.LlmClientProvider;
import dev.ervinszilagyi.ai.mcpserver.McpLogMessageHandlerProvider;
import dev.ervinszilagyi.ai.memory.ChatMemoryProvider;
import dev.ervinszilagyi.commands.list.ListMcpDetails;
import dev.ervinszilagyi.commands.session.ChatModelListenerProvider;
import dev.ervinszilagyi.commands.session.ChatSession;
import dev.ervinszilagyi.config.llm.LlmConfig;
import dev.ervinszilagyi.config.llm.LlmConfigModule;
import dev.ervinszilagyi.config.mcp.McpConfigModule;
import dev.ervinszilagyi.md.StylizedPrinter;
import dev.ervinszilagyi.terminal.TerminalModule;
import dev.langchain4j.mcp.client.McpClient;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jline.terminal.Terminal;

import java.io.File;
import java.util.Map;

@Component(modules = {
        McpConfigModule.class,
        LlmConfigModule.class,
        TerminalModule.class,
        ChatModelProvider.class,
        ChatModelListenerProvider.class,
        ChatMemoryProvider.class,
        LlmClientProvider.class,
        McpLogMessageHandlerProvider.class
})
@Singleton
public interface AppComponent {
    StylizedPrinter stylizedPrinter();

    LlmConfig llmConfig();

    Terminal terminal();

    ChatModelWithInfo chatModelWithInfo();

    Map<String, McpClient> mcpClients();

    LlmClient llmClient();

    ChatSession chatSession();

    ListMcpDetails listMcpDetails();

    @Component.Factory
    interface Factory {
        AppComponent create(@BindsInstance @Named("mcpConfig") File mcpConfig,
                            @BindsInstance @Named("llmConfig") File llmConfig);
    }
}
