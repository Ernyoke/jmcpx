package dev.ervinszilagyi.session;

public enum SessionCommand {
    EXIT("/exit"), NEW("/new"), HELP("/help");

    final String commandStr;

    SessionCommand(String input) {
        this.commandStr = input;
    }

    public boolean isCommand(String input) {
        return commandStr.equals(input);
    }
}
