This is a modification of the [CodinGame Game Skeleton](https://github.com/CodinGame/game-skeleton/) made to work with a game written in an arbitrary language. It leverages the existing players from [CodinGame Game-Tictactoe](https://github.com/CodinGame/game-tictactoe) and borrows some elements (agents, commands) from the [CodinGame Game Engine](https://github.com/CodinGame/codingame-game-engine). More information about those repositories can be found in the [CodinGame sdk docs repo](https://github.com/CodinGame/codingame-sdk-doc).


To specify the game to run, add a `test.properties` file in the root directory with comma separated command line arguments. For example:

```
commandargs=node,C:\\path\\to\\index.js
```

See `typescript/referee` for an example of a referee for tic-tac-toe implemented in typescript.

To run the game, run `src/test/java/Main.java` which will execute the game and launch a sever at http://localhost:8888 to view the output.