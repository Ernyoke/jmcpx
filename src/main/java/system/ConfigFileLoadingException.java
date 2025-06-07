package system;

import java.io.File;

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
