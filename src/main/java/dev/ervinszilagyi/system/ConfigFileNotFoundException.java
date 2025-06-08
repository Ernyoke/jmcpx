package dev.ervinszilagyi.system;

import java.io.File;

/**
 * Exception thrown when a configuration file is not found.
 * This exception is used to indicate that a specific configuration file could not be located
 * in the expected path.
 */
public class ConfigFileNotFoundException extends RuntimeException {
    private final File file;

    public ConfigFileNotFoundException(final File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
