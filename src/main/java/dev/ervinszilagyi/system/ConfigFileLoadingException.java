package dev.ervinszilagyi.system;

import java.io.File;

/**
 * Exception thrown when a configuration file cannot be loaded.
 */
public class ConfigFileLoadingException extends RuntimeException {
    private final File file;
    private final String userFacingReason;

    public ConfigFileLoadingException(final Throwable cause, final String userFacingReason, final File file) {
        super(cause);
        this.userFacingReason = userFacingReason;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getUserFacingReason() {
        return userFacingReason;
    }
}
