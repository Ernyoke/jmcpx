package dev.ervinszilagyi.system;

/**
 * Exception thrown when an invalid LLM configuration is encountered.
 * This could be due to missing required fields, unsupported configurations,
 * or any other validation issues related to the LLM configuration.
 */
public class InvalidLlmConfigException extends RuntimeException {
    public InvalidLlmConfigException(String message) {
        super(message);
    }
}
