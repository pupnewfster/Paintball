package net.battlenexus.paintball.game.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Stack;

public class ConfigWriter {
    private ArrayList<String> lines = new ArrayList<>();
    private boolean closed;
    private Stack<String> objectStack = new Stack<>();

    public void addConfig(String name, boolean value) {
        addConfig("name", value ? "true" : "false");
    }

    public void addConfig(String name, Object value) {
        if (closed)
            throw new IllegalStateException("ConfigWriter already closed!");

        lines.add("<" + name + ">" + value.toString() + "</" + name + ">");
    }

    public void begin() {
        lines.clear();
        lines.add("<mapConfig>");
        closed = false;
    }

    public void beginObject(String objectName) {
        objectStack.add(objectName);
        lines.add("<" + objectStack.peek() + ">");
    }

    public void endObject() {
        if (objectStack.isEmpty())
            throw new IllegalStateException("No object started!");

        lines.add("</" + objectStack.pop() + ">");
    }

    public boolean isClosed() {
        return closed;
    }

    public void end() {
        lines.add("</mapConfig>");

        closed = true;
    }

    public void saveToFile(File file) throws IOException {
        if (!closed)
            end();

        Formatter formatter = new Formatter(new FileWriter(file, true));
        for (String line : lines)
            formatter.out().append(line).append("\n");
        formatter.close();
    }
}
