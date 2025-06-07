package dev.ervinszilagyi.terminal;

import dagger.Module;
import dagger.Provides;
import dev.ervinszilagyi.config.mcp.McpConfigProvider;
import jakarta.inject.Singleton;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.TerminalProvisioningException;

import java.io.IOException;

@Module
public class TerminalModule {
    private static final Logger logger = LoggerFactory.getLogger(TerminalModule.class);

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
            logger.error("Could not create ANSI terminal:", ioException);
            throw new TerminalProvisioningException(ioException);
        }
    }
}
