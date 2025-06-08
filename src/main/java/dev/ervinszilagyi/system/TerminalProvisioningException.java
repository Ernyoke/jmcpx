package dev.ervinszilagyi.system;

/**
 * Exception thrown when there is an error during terminal provisioning.
 * This could be due to issues like missing dependencies or configuration errors.
 */
public class TerminalProvisioningException extends RuntimeException {
    public TerminalProvisioningException(final Throwable cause) {
        super(cause);
    }
}
