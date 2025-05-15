package dev.ervinszilagyi.session;

public enum SessionCommand {
    EXIT("/exit", "Exit from the current session."),
    NEW("/new", "Clear history and create a new session."),
    HELP("/help", "Get help information.");

    final String commandStr;
    final String helpMessage;

    SessionCommand(String input, String helpMessage) {
        this.commandStr = input;
        this.helpMessage = helpMessage;
    }

    public boolean isCommand(String input) {
        return commandStr.equals(input);
    }
}
