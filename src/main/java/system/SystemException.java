package system;

/**
 * Runtime exception whose purpose is to handle system-wide exceptions. These exceptions are maine un-recoverable.
 */
public class SystemException extends RuntimeException {
    private final ErrorType errorType;

    public enum ErrorType {
        LLM_TOML_NOT_FOUND,
        LLM_TOML_COULD_NOT_BE_LOADED,
        MCP_CONFIG_NOT_FOUND,
        MCP_CONFIG_COULD_NOT_BE_LOADED,
        TERMINAL_COULD_NOT_CREATED
    }

    public SystemException(final ErrorType errorType, final String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
