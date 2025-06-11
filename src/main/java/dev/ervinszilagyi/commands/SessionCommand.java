package dev.ervinszilagyi.commands;

import dev.ervinszilagyi.app.AppComponent;
import dev.ervinszilagyi.app.DaggerAppComponent;
import dev.ervinszilagyi.system.ConfigFileLoadingException;
import dev.ervinszilagyi.system.ConfigFileNotFoundException;
import dev.ervinszilagyi.system.TerminalProvisioningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "session", description = "Start MCP session.")
public class SessionCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--mcp"}, description = "Location of the mcp.json file.", defaultValue = "${sys:user.home}/jmcpx/mcp.json")
    private File mcpLocation;

    @CommandLine.Option(names = {"-l", "--llm"}, description = "Location of the llm.toml file.", defaultValue = "${sys:user.home}/llm.toml")
    private File llmConfigLocation;

    @CommandLine.Option(names = {"-v", "--verboseLogging"}, description = "Enable detailed logging such as requests/" +
            "responses from LLM models.", defaultValue = "false")
    private boolean detailedLoggingEnabled;

    private static final Logger logger = LoggerFactory.getLogger(SessionCommand.class);

    @Override
    public void run() {
        try {
            AppComponent appComponent = DaggerAppComponent.factory()
                    .create(mcpLocation, llmConfigLocation, detailedLoggingEnabled);
            appComponent.chatSession().openSession();
        } catch (final ConfigFileLoadingException configFileLoadingException) {
            System.err.println("Config file " + configFileLoadingException.getFile() + " could not be loaded.");
        } catch (final ConfigFileNotFoundException configFileNotFoundException) {
            System.err.println("Config file " + configFileNotFoundException.getFile() + " cannot be found.");
        } catch (final TerminalProvisioningException terminalProvisioningException) {
            System.err.println("ANSI Terminal could not be created.");
        }
    }
}
