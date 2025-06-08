package dev.ervinszilagyi.md;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.PrintWriter;

/**
 * StylizedPrinter is a utility class for printing stylized messages to the terminal.
 * It uses Flexmark for Markdown parsing and formatting, and JLine for terminal output.
 */
@Singleton
public class StylizedPrinter {
    private final Parser parser;
    private final Formatter formatter;

    private final Terminal terminal;

    @Inject
    public StylizedPrinter(final Terminal terminal) {
        this.terminal = terminal;

        DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
                Extensions.ALL
        );

        MutableDataSet FORMAT_OPTIONS = new MutableDataSet();
        FORMAT_OPTIONS.set(Parser.EXTENSIONS, Parser.EXTENSIONS.get(OPTIONS));

        parser = Parser.builder(OPTIONS).build();
        formatter = Formatter.builder(FORMAT_OPTIONS).build();
    }

    public void printMarkDown(final String content) {
        Node document = parser.parse(content);
        String commonmark = formatter.render(document);

        PrintWriter writer = terminal.writer();
        writer.println(commonmark);
        writer.flush();
    }

    public void printError(final String error) {
        AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                .append("Error: ").append(error).append("\n");

        PrintWriter writer = terminal.writer();
        writer.write(attributedStringBuilder.toAnsi(terminal));
        writer.flush();
    }

    public void printSystemMessage(final String message) {
        AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
                .append(message).append("\n");

        PrintWriter writer = terminal.writer();
        writer.write(attributedStringBuilder.toAnsi(terminal));
        writer.flush();
    }

    public void printInfoMessage(final String message) {
        AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE))
                .append(message).append("\n");

        PrintWriter writer = terminal.writer();
        writer.write(attributedStringBuilder.toAnsi(terminal));
        writer.flush();
    }
}
