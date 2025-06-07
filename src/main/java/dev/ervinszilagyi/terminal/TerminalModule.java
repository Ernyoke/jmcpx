package dev.ervinszilagyi.terminal;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import system.SystemException;

import java.io.IOException;

@Module
public class TerminalModule {
    @Provides
    @Singleton
    public Terminal provideTerminal() {
        try {
            return TerminalBuilder.builder()
                    .jansi(true)
                    .jna(true)
                    .system(true)
                    .build();
        } catch (IOException ioException) {
            throw new SystemException(SystemException.ErrorType.TERMINAL_COULD_NOT_CREATED, ioException.getMessage());
        }
    }
}
