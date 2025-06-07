package system;

import java.io.File;

public class ConfigFileNotFoundException extends RuntimeException {
    private final File file;

    public ConfigFileNotFoundException(final File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
