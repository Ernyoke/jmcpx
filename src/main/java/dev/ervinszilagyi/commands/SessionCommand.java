package dev.ervinszilagyi.commands;

import dev.ervinszilagyi.app.AppComponent;
import dev.ervinszilagyi.app.DaggerAppComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import system.SystemException;

import java.io.File;

@CommandLine.Command(name = "session", description = "Start MCP session.")
public class SessionCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--mcp"}, description = "Location of the mcp.json file.", defaultValue = "${sys:user.home}/jmcpx/mcp.json")
    private File mcpLocation;

    @CommandLine.Option(names = {"-l", "--llm"}, description = "Location of the llm.toml file.", defaultValue = "${sys:user.home}/llm.toml")
    private File llmConfigLocation;

    @CommandLine.Option(names = {"-d", "--debug"}, description = "Run application is debug mode with. This will enable " +
            "enhanced logging for each request and response to LLMs.")
    private boolean debugMode;

    private static final Logger logger = LoggerFactory.getLogger(SessionCommand.class);

    @Override
    public void run() {
        try {
            AppComponent appComponent = DaggerAppComponent.factory()
                    .create(mcpLocation, llmConfigLocation);
            appComponent.chatSession().openSession();
        } catch (SystemException systemException) {
            switch (systemException.getErrorType()) {
                case TERMINAL_COULD_NOT_CREATED -> System.out.println(
                        "Terminal could not be created: " + systemException.getMessage()
                );
                case LLM_TOML_COULD_NOT_BE_LOADED -> System.out.println(
                        "LLM config could not be loaded: " + systemException.getMessage()
                );
                case MCP_CONFIG_COULD_NOT_BE_LOADED -> System.out.println(
                        "MCP config could not be loaded: " + systemException.getMessage()
                );
                default -> System.out.println(systemException.getMessage());
            }
        }
    }
}
