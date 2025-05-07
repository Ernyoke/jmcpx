package dev.ervinszilagyi;

import dev.ervinszilagyi.commands.InitCommand;
import picocli.CommandLine;

public class App {
    public static void main(String[] args) {
//        System.setProperty("org.jline.logging.output", "System.err");
//        System.setProperty("org.jline.logging.level", "DEBUG");

        int exitCode = new CommandLine(new InitCommand()).setExecutionStrategy(new CommandLine.RunAll()).execute(args);
        System.exit(exitCode);
    }
}
