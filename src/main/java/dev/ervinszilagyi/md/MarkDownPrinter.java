package dev.ervinszilagyi.md;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.PrintWriter;

public class MarkDownPrinter {
    private final Parser parser;
    private final Formatter formatter;

    public MarkDownPrinter() {
        DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
                Extensions.ALL
        );

        MutableDataSet FORMAT_OPTIONS = new MutableDataSet();
        FORMAT_OPTIONS.set(Parser.EXTENSIONS, Parser.EXTENSIONS.get(OPTIONS));

        parser = Parser.builder(OPTIONS).build();
        formatter = Formatter.builder(FORMAT_OPTIONS).build();
    }

    public void print(String content, PrintWriter writer) {
        Node document = parser.parse(content);
        String commonmark = formatter.render(document);
        writer.println(commonmark);
    }
}
