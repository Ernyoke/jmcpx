package dev.ervinszilagyi.commands;

import dev.ervinszilagyi.app.AppComponent;
import dev.ervinszilagyi.app.DaggerAppComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "list", description = "List details about available MCP servers.")
public class ListDetailsCommand implements Runnable {
    @CommandLine.Option(names = {"-c", "--mcp"}, description = "Location of the mcp.json file.", defaultValue = "${sys:user.home}/jmcpx/mcp.json")
    private File mcpLocation;

    @CommandLine.Option(names = {"-l", "--llm"}, description = "Location of the llm.toml file.", defaultValue = "${sys:user.home}/llm.toml")
    private File llmConfigLocation;

    private static final Logger logger = LoggerFactory.getLogger(ListDetailsCommand.class);

    @Override
    public void run() {
        AppComponent appComponent = DaggerAppComponent.factory()
                .create(mcpLocation, llmConfigLocation);

        appComponent.listMcpDetails().displayDetails(null);
    }
}
