package com.codingame.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

class Command {

    interface CommandKey {
        String name();
    }

    static enum OutputCommand implements CommandKey {
        INIT,
        GAME_TURN,
        ON_END,
        GET_PLAYER_OUTPUTS,
        GET_PLAYERS,
    }

    static enum InputCommand implements CommandKey {
        SEND_PLAYER_INPUT,
        EXECUTE_PLAYER,
        DEACTIVATE_PLAYER,
        SET_PLAYER_SCORE,
        END_INIT,
        END_TURN,
        END_GAME,
        ADD_SUMMARY,
        ADD_SUMMARY_SUCCESS,
        ADD_SUMMARY_ERROR,
        GET_PLAYER_OUTPUTS,
        GET_PLAYERS,
    }

    // static enum InputCommand implements CommandKey {
    //     VIEW, INFOS, NEXT_PLAYER_INPUT, NEXT_PLAYER_INFO, SCORES, UINPUT, TOOLTIP, SUMMARY, METADATA;
    // }

    private List<String> lines;
    private CommandKey key;

    public Command(CommandKey key) {
        this.key = key;
        lines = new ArrayList<>();
    }

    public Command(CommandKey key, String... lines) {
        this.key = key;
        this.lines = Arrays.asList(lines);
    }

    public void addLine(Object data) {
        lines.add(String.valueOf(data));

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[[%s] %d]", key.name(), lines.size()));
        sb.append('\n');
        sb.append(lines.stream().map(line -> line + "\n").collect(Collectors.joining()));
        return sb.toString();

    }

    CommandKey getKey() {
        return key;
    }

    List<String> getLines() {
        return lines;
    }
}