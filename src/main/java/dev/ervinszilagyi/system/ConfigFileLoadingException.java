package dev.ervinszilagyi.system;

import java.io.File;

/**
 * Exception thrown when a configuration file cannot be loaded.
 */
public class ConfigFileLoadingException extends RuntimeException{
    private final File file;

    public ConfigFileLoadingException(final Throwable cause, final File file) {
        super(cause);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
