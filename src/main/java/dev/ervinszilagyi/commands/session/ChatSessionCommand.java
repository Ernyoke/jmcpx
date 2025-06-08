package dev.ervinszilagyi.commands.session;

import java.util.HashSet;
import java.util.Set;

/**
 * Commands that can be used inside a chat session.
 */
public enum ChatSessionCommand {
    EXIT("/exit", Set.of(), "Exit from the current session."),
    NEW("/new", Set.of("/clear"), "Clear history and create a new session."),
    HELP("/help", Set.of(), "Get help information."),
    TOOLS("/tools", Set.of(), "List available tools."),
    PROMPTS("/prompts", Set.of(), "List available prompts");

    final String commandStr;
    final Set<String> aliases;
    final String helpMessage;

    /**
     * Constructor for a command.
     *
     * @param commandStr  String equivalent of the command.
     * @param aliases     Aliases for the command. Example, we can clear the chat history with the "/new" command.
     *                    Additionally, we can accomplish the same thing with the "/clear".
     * @param helpMessage Documentation for the command.
     */
    ChatSessionCommand(String commandStr, Set<String> aliases, String helpMessage) {
        this.commandStr = commandStr;
        this.aliases = new HashSet<>(aliases);
        this.helpMessage = helpMessage;
    }

    /**
     * Checks if the input provided by the user is a command.
     *
     * @param input User input.
     * @return true of the input is a command.
     */
    public boolean isCommand(final String input) {
        return commandStr.equals(input) || aliases.contains(input);
    }

    /**
     * Returns documentation for the command. The documentation includes the name of the command with all the aliases
     * and the help message.
     *
     * @return documentation to be displayed for the user.
     */
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
