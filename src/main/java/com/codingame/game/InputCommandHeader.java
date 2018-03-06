package com.codingame.game;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class InputCommandHeader {

    public final Command.CommandKey key;
    public final int numLines;

    public InputCommandHeader(Command.CommandKey key, int numLines) {
        this.key = key;
        this.numLines = numLines;
    }

    public static InputCommandHeader parse(String header) {
        final Pattern HEADER_PATTERN = Pattern.compile("\\[\\[(?<cmd>.+)\\] ?(?<lineCount>[0-9]+)\\]");

        Matcher m = HEADER_PATTERN.matcher(header);
        if (!m.matches()) {
            return null;
        }

        try {
            Command.CommandKey cmd = Command.InputCommand.valueOf(m.group("cmd"));
            int lineCount = Integer.parseInt(m.group("lineCount"));
            return new InputCommandHeader(cmd, lineCount);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}