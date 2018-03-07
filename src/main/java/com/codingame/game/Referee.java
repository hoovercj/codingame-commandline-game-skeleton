package com.codingame.game;

import java.util.Properties;
import java.util.List;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.game.Command.InputCommand;
import com.codingame.game.Command.OutputCommand;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.google.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Referee extends AbstractReferee {
    @Inject private GameManager<Player> gameManager;
    private Agent referee;

    private static Log log = LogFactory.getLog(Agent.class);

    // @Inject private GraphicEntityModule graphicEntityModule;
    private int[][] grid = new int[3][3];

    private static final int CELL_SIZE = 250;
    private static final int LINE_WIDTH = 10;
    private static final int LINE_COLOR = 0xff0000;
    private static final int GRID_ORIGIN_Y = (int) Math.round(1080 / 2 - CELL_SIZE);
    private static final int GRID_ORIGIN_X = (int) Math.round(1920 / 2 - CELL_SIZE);

    private static final String COMMANDARGS_PROPERTY = "refereecommandargs";

    @Override
    public Properties init(Properties params) {

        String[] commandArgs = params.getProperty(COMMANDARGS_PROPERTY).split(",");
        params.remove(COMMANDARGS_PROPERTY);
        referee = new CommandLineRefereeAgent(commandArgs);
        referee.execute();

        Command initCommand = new Command(OutputCommand.INIT);
        params.forEach((key, value) -> initCommand.addLine(key + " " + value));
        referee.sendInput(initCommand.toString());

        Command command;
        while((command = readCommand()).getKey() != InputCommand.END_INIT) {
            processCommand(command);
        }

        return params;
    }

    @Override
    public void gameTurn(int turn) {
        Command gameTurnCommand = new Command(OutputCommand.GAME_TURN);
        gameTurnCommand.addLine(turn);
        referee.sendInput(gameTurnCommand.toString());

        Command command;
        while((command = readCommand()).getKey() != InputCommand.END_TURN) {
            processCommand(command);
        }
    }

    // TODO: Support this on the client
    // @Override
    // public void onEnd() {
    //     Command gameEndCommand = new Command(OutputCommand.ON_END);
    //     referee.sendInput(gameEndCommand.toString());
    // }

    private Command readCommand() {
        Command command = null;
        while (command == null) {
            String output = referee.getOutput(1, gameManager.getTurnMaxTime()).trim();

            InputCommandHeader header = InputCommandHeader.parse(output);

            if (header == null) {
                continue;
            }

            command = new Command(header.key);

            if (header.numLines > 0) {
                String[] outputLines = referee.getOutput(header.numLines, gameManager.getTurnMaxTime()).split("\n");
                for (String s : outputLines) {
                    command.addLine(s);
                }
            }
        }
        return command;
    }

    private void processCommand(Command command) {
        Command.CommandKey key = command.getKey();
        List<String> lines = command.getLines();

        // TODO: error handling

        // Game related commands:
        if (key == InputCommand.END_GAME) {
            gameManager.endGame();
            return;
        }

        if (key == InputCommand.ADD_SUMMARY) {
            for (String s : lines) {
                gameManager.addToGameSummary(s);
            }
            return;
        } else if (key == InputCommand.ADD_SUMMARY_ERROR) {
            for (String s : lines) {
                gameManager.addToGameSummary(GameManager.formatErrorMessage(s));
            }
            return;
        } else if (key == InputCommand.ADD_SUMMARY_SUCCESS) {
            for (String s : lines) {
                gameManager.addToGameSummary(GameManager.formatSuccessMessage(s));
            }
            return;
        }

        if (key == InputCommand.GET_PLAYERS) {
            Command getPlayersCommand = new Command(OutputCommand.GET_PLAYERS);
            for (int i = 0; i < gameManager.getPlayerCount(); i++) {
                Player player = gameManager.getPlayer(i);
                getPlayersCommand.addLine(player.getNicknameToken());
            }

            referee.sendInput(getPlayersCommand.toString());
            return;
        }

        // Player Related Commands:
        Player player = gameManager.getPlayer(Integer.parseInt(lines.get(0)));
        if (key == InputCommand.SEND_PLAYER_INPUT) {
            for (String s : lines.subList(1,lines.size())) {
                player.sendInputLine(s);
            }
            return;
        } else if (key == InputCommand.SEND_PLAYER_INPUT) {
            player.execute();
            return;
        } else if (key == InputCommand.DEACTIVATE_PLAYER) {
            if (lines.size() > 1) {
                player.deactivate(lines.get(1));
            } else {
                player.deactivate();
            }
            return;
        } else if (key == InputCommand.SET_PLAYER_SCORE) {
            player.setScore(Integer.parseInt(lines.get(1)));
            return;
        } else if (key == InputCommand.GET_PLAYER_OUTPUTS) {
            try {
                String output = player.getOutputs().get(0);
                Command getPlayerOutputsCommand = new Command(OutputCommand.GET_PLAYER_OUTPUTS);
                getPlayerOutputsCommand.addLine(output);
                referee.sendInput(getPlayerOutputsCommand.toString());
            } catch (Exception e) {
                log.error(e.getMessage());
                // TODO: do something
            }
            return;
        } else if (key == InputCommand.EXECUTE_PLAYER) {
            player.execute();
            return;
        }

        throw new UnsupportedOperationException("Command unsupported: " + key);
    }

    // TODO: Move to chosen language
    private void initPlayers() {
        for (Player player : gameManager.getPlayers()) {
            player.sendInputLine(String.format("%d", player.getIndex() + 1));
        }
    }

    // TODO: Move to chosen language
    private int checkWinner() {
        for (int i = 0; i < 3; i++) {
            // check rows
            if (grid[i][0] > 0 && grid[i][0] == grid[i][1] && grid[i][0] == grid[i][2]) {
                // drawVictoryLine(i, 0, i, 2, gameManager.getPlayer(grid[i][0] - 1));
                return grid[i][0];
            }

            // check cols
            if (grid[0][i] > 0 && grid[0][i] == grid[1][i] && grid[0][i] == grid[2][i]) {
                // drawVictoryLine(0, i, 2, i, gameManager.getPlayer(grid[0][i] - 1));
                return grid[0][i];
            }
        }

        // check diags
        if (grid[0][0] > 0 && grid[0][0] == grid[1][1] && grid[0][0] == grid[2][2]) {
            // drawVictoryLine(0, 0, 2, 2, gameManager.getPlayer(grid[0][0] - 1));
            return grid[0][0];
        }
        if (grid[2][0] > 0 && grid[2][0] == grid[1][1] && grid[2][0] == grid[0][2]) {
            // drawVictoryLine(2, 0, 0, 2, gameManager.getPlayer(grid[1][1] - 1));
            return grid[2][0];
        }

        return 0;
    }

    // TODO: MOve to chosen language
    private void doGameTurn(int turn) {
        Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());

        // Send inputs
        for (int l = 0; l < 3; l++) {
            player.sendInputLine(String.format("%d %d %d", grid[l][0], grid[l][1], grid[l][2]));
        }
        player.execute();

        // Read inputs
        try {
            String[] output = player.getOutputs().get(0).split(" ");
            int targetRow = Integer.parseInt(output[0]);
            int targetCol = Integer.parseInt(output[1]);

            if (targetRow < 0 || targetRow >= 3 || targetCol < 0 || targetCol >= 3 || grid[targetRow][targetCol] != 0) {
                player.deactivate("Invalid action.");
                player.setScore(-1);
                gameManager.endGame();
            } else {
                // Sprite avatar = graphicEntityModule.createSprite()
                //         .setX(convertX(targetCol))
                //         .setY(convertY(targetRow))
                //         .setImage(player.getAvatarToken())
                //         .setAnchor(0.5);

                // // Animate arrival
                // avatar.setScale(0);
                // graphicEntityModule.commitEntityState(0, avatar);
                // avatar.setScale(1, Curve.ELASTIC);
                // graphicEntityModule.commitEntityState(1, avatar);

            }

            gameManager.addToGameSummary(String.format("Player %s played (%d %d)", player.getNicknameToken(), targetRow, targetCol));

            // update grid
            grid[targetRow][targetCol] = player.getIndex() + 1;

        } catch (NumberFormatException e) {
            player.deactivate("Wrong output!");
            player.setScore(-1);
            gameManager.endGame();
        } catch (TimeoutException e) {
            gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " timeout!"));
            player.deactivate(player.getNicknameToken() + " timeout!");
            player.setScore(-1);
            gameManager.endGame();
        }

        // check winner - LOCAL CODE
        int winner = checkWinner();
        if (winner > 0) {
            gameManager.addToGameSummary(GameManager.formatSuccessMessage(player.getNicknameToken() + " won!"));
            gameManager.getPlayer(winner - 1).setScore(1);
            gameManager.endGame();
        }
    }
}
