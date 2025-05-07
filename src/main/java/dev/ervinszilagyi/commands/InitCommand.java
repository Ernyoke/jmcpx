package dev.ervinszilagyi.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "jmcpx", mixinStandardHelpOptions = true, version = "0.1.0",
        subcommands = {SessionCommand.class, ListDetailsCommand.class},
        description = "MCP client implemented in Java.")
public class InitCommand implements Callable<Integer> {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    private static final Logger logger = LoggerFactory.getLogger(InitCommand.class);

    @Override
    public Integer call() throws IOException {
        logger.info("Application started.");

        CommandLine.ParseResult parseResult = spec.commandLine().getParseResult();
        if (!parseResult.hasSubcommand()) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
        }

        return 0;
    }
}
