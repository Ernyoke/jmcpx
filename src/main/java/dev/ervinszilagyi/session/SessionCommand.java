package dev.ervinszilagyi.session;

import java.util.HashSet;
import java.util.Set;

public enum SessionCommand {
    EXIT("/exit", Set.of(), "Exit from the current session."),
    NEW("/new", Set.of("/clear"), "Clear history and create a new session."),
    HELP("/help", Set.of(), "Get help information.");

    final String commandStr;
    final Set<String> aliases;
    final String helpMessage;

    SessionCommand(String input, Set<String> aliases, String helpMessage) {
        this.commandStr = input;
        this.aliases = new HashSet<>(aliases);
        this.helpMessage = helpMessage;
    }

    public boolean isCommand(String input) {
        return commandStr.equals(input) || aliases.contains(input);
    }

    public String getDocumentation() {
        StringBuilder sb = new StringBuilder();
        sb.append(commandStr).append(" ");
        if (!aliases.isEmpty()) {
            sb.append("(").append(String.join(", ", aliases)).append(")");
        }
        sb.append(": ").append(helpMessage);

        return sb.toString();
    }
}
