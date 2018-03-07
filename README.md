# Commandlin Game Skeleton

## Overview
This is a modification of the [CodinGame Game Skeleton](https://github.com/CodinGame/game-skeleton/) made to work with a game written in an arbitrary language. It leverages the existing players from [CodinGame Game-Tictactoe](https://github.com/CodinGame/game-tictactoe) and borrows some elements (agents, commands) from the [CodinGame Game Engine](https://github.com/CodinGame/codingame-game-engine). More information about those repositories can be found in the [CodinGame sdk docs repo](https://github.com/CodinGame/codingame-sdk-doc).


Configure the game and players by adding a `test.properties` file in the root directory.

If the referee is a command line referee, add a comma separated list of parameters to execute the process with:
```
refereecommandargs=node,C:\\path\\to\\index.js # Note: comma separated
```
See `typescript/referee` for an example of a referee for tic-tac-toe implemented in typescript.

Players should be specified by adding properties starting with the word `player`.

The values can be in two forms:
* `cli,<COMMAND>`: Note that the command here is not a comma separated string of arguments but rather a single string. Making it a comma separated list of arguments would require modifying the game engine project
* `java,<CLASSNAME>`: This is the string needed to get the java class by calling `Class.forName()`

```
player1=cli,C:\\path\\to\spidermonkey C:\\path\\to\\index.js # 
player2=java,player2.Player
```

See `src/test/java/player3/player.js` for an example of a player written in javascript for the spidermonkey shell.

See `src/test/java/player1/Player.java` for an example of a player written in Java.

To run the game, run `src/test/java/Main.java` which will execute the game and launch a server at http://localhost:8888 to view the output.


## Roadmap
* Configuration
    * Referee
        * [X] Specify command
        * [ ] Specify java class
    * Players
        * [X] Specify command
        * [X] Specify java class
* Project creation
    * CLI Referee - without visual assets
        * Input: A file in language X
        * Process: build, bundle, etc.
        * Output:
            * Processed files in a directory or a zip
            * A run command relative to the bundle (e.g. "node path/to/index.js")
    * CLI Players
        * Input: A file in language X
        * Process: build, bundle, etc.
        * Output:
            * Processed files in a directory or a zip
            * A run command relative to the bundle (e.g. "node path/to/index.js")
    * CLI-Only "Runnable" solution
        * Input: Referee and Player bundles + run commands
        * Extract each bundle and create game runner config
            * CWD for each command
            * Command
        * Output: Config which can be read by game runner solution and will run game with provided referee and player
* Game Execution
    * Input:
        * This solution
        * Config pointing to referee and players
    * Execute jar by passing path to config
    * Output:
        * game.json file