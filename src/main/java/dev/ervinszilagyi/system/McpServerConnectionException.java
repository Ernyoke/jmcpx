package dev.ervinszilagyi.system;

public class McpServerConnectionException extends RuntimeException {
    private final String serverName;

    public McpServerConnectionException(final Throwable cause, String serverName) {
        super(cause);
        this.serverName = serverName;
    }
}
