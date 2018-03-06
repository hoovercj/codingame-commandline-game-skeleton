"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const readline = require("readline");
const fs = require("fs");
const path = require("path");
const logPath = path.join(process.cwd(), 'NODE_LOG.txt');
try {
    fs.unlinkSync(logPath);
}
catch (_a) { }
function log(text) {
    // fs.appendFileSync(logPath, text + '\n');
}
const commandKeyPattern = new RegExp(/\[\[(.+)\] ?([0-9]+)\]/);
var OutputCommand;
(function (OutputCommand) {
    OutputCommand["SEND_PLAYER_INPUT"] = "SEND_PLAYER_INPUT";
    OutputCommand["EXECUTE_PLAYER"] = "EXECUTE_PLAYER";
    OutputCommand["DEACTIVATE_PLAYER"] = "DEACTIVATE_PLAYER";
    OutputCommand["SET_PLAYER_SCORE"] = "SET_PLAYER_SCORE";
    OutputCommand["END_INIT"] = "END_INIT";
    OutputCommand["END_TURN"] = "END_TURN";
    OutputCommand["END_GAME"] = "END_GAME";
    OutputCommand["ADD_SUMMARY"] = "ADD_SUMMARY";
    OutputCommand["ADD_SUMMARY_SUCCESS"] = "ADD_SUMMARY_SUCCESS";
    OutputCommand["ADD_SUMMARY_ERROR"] = "ADD_SUMMARY_ERROR";
    OutputCommand["GET_PLAYER_OUTPUTS"] = "GET_PLAYER_OUTPUTS";
    OutputCommand["GET_PLAYERS"] = "GET_PLAYERS";
})(OutputCommand || (OutputCommand = {}));
var InputCommand;
(function (InputCommand) {
    InputCommand["INIT"] = "INIT";
    InputCommand["GAME_TURN"] = "GAME_TURN";
    InputCommand["ON_END"] = "ON_END";
    InputCommand["GET_PLAYER_OUTPUTS"] = "GET_PLAYER_OUTPUTS";
    InputCommand["GET_PLAYERS"] = "GET_PLAYERS";
})(InputCommand || (InputCommand = {}));
const stdin = process.stdin;
const stdout = process.stdout;
const lineReader = readline.createInterface({
    input: stdin,
    output: stdout,
    terminal: false
});
let stdinLineBuffer = [];
let _onStdinLine;
lineReader.on('line', stdinListener);
// players[index] = nickname
const players = [];
const grid = [[0, 0, 0], [0, 0, 0], [0, 0, 0]];
let gameOver = false;
// Game loop
start();
function start() {
    return __awaiter(this, void 0, void 0, function* () {
        log("Game started");
        yield initGame();
        log("Init finished");
        while (!gameOver) {
            yield processCommand(InputCommand.GAME_TURN, (line, index) => __awaiter(this, void 0, void 0, function* () {
                try {
                    yield doGameTurn(parseInt(line));
                    return true;
                }
                catch (_a) {
                    return false;
                }
            }));
        }
    });
}
///
/// Command related functions
///
function sendCommand(command, lines) {
    if (stdout.writable) {
        let commandKey = `[${command.toString()}]`;
        let output;
        if (typeof (lines) === 'string' || typeof (lines) === 'number') {
            output = `[${commandKey} ${1}]\n${lines}`;
        }
        else if (Array.isArray(lines)) {
            output = `[${commandKey} ${lines.length}]\n${lines.join('\n')}`;
        }
        else {
            output = `[${commandKey} ${0}]`;
        }
        log('Sending command: ' + output);
        stdout.write(`${output}\n`);
    }
    else {
        log('stdout is not writeable, cannot send command ' + command);
    }
}
function processCommand(expectedCommand, lineHandler) {
    return __awaiter(this, void 0, void 0, function* () {
        log(`Process ${expectedCommand.toString()}...`);
        const promise = new Promise((resolve, reject) => {
            setOnStdinLine((line) => {
                setOnStdinLine(undefined);
                line = line.trim();
                // First line received should be: [[<COMMAND>] <NUM_LINES>]
                let matches = commandKeyPattern.exec(line);
                if (matches && matches.length >= 3) {
                    const [receivedCommand, linesString] = matches.slice(1);
                    log(`Command: ${receivedCommand}`);
                    log(`Num lines: ${linesString}`);
                    if (!assertCommand(expectedCommand, receivedCommand)) {
                        reject();
                    }
                    let numLines = parseInt(linesString, 10);
                    if (numLines === 0) {
                        return resolve();
                    }
                    let lineIndex = 0;
                    setOnStdinLine((line) => __awaiter(this, void 0, void 0, function* () {
                        log(`Read line ${lineIndex}: ${line}`);
                        if (lineHandler) {
                            log(`Calling handler with line ${lineIndex}: ${line}`);
                            const success = yield lineHandler(line, lineIndex++);
                            if (!success) {
                                log(`Line handler failed`);
                                setOnStdinLine(undefined);
                                return reject();
                            }
                        }
                        else {
                            log('Ignoring line');
                            lineIndex++;
                        }
                        log(`Processed line ${lineIndex - 1}`);
                        if (lineIndex === numLines) {
                            log(`Processed all ${numLines} lines`);
                            setOnStdinLine(undefined);
                            return resolve();
                        }
                    }));
                }
                else {
                    log(`Regex match failed. Matches: ${matches && matches.join(', ')}`);
                    const message = `Expected ${expectedCommand} but received ${line}`;
                    log(message);
                    reject(message);
                }
            });
        });
        return promise;
    });
}
function assertCommand(expected, actual) {
    if (expected === actual) {
        return true;
    }
    const message = `Assert Command Failed: Expected ${expected} but received ${actual}`;
    log(message);
    sendCommand(OutputCommand.ADD_SUMMARY_ERROR, message);
    endGame();
    // exitProcessSafe(); // TODO: do I need to exit the process?
    return false;
}
///
/// Stdin Functions
///
function setOnStdinLine(handler) {
    _onStdinLine = handler;
    if (!handler) {
        log(`Cleared stdin handler`);
        return;
    }
    log('Set new stdin handler');
    // When a handler is assigned, use it to empty the buffer
    // of lines that were received when there was no handler
    while (stdinLineBuffer.length > 0) {
        log(`Clearing stdin buffer...`);
        let line = stdinLineBuffer.shift();
        if (!isNullOrEmpty(line)) {
            log(`Calling handler with ${line}`);
            handler(line);
        }
    }
}
function stdinListener(line) {
    const trimmedLine = line.trim();
    if (isNullOrEmpty(trimmedLine)) {
        log('stdinListener: Received null or empty line');
        return;
    }
    if (_onStdinLine) {
        _onStdinLine(trimmedLine);
    }
    else {
        log('stdinListener: Buffering line: ' + trimmedLine);
        // These lines will be consumed when _onStdinData
        // is next assigned
        stdinLineBuffer.push(trimmedLine);
    }
}
function exitProcessSafe(exitCode = 0) {
    process.exitCode = exitCode;
    lineReader.close();
}
///
/// Game Functions
///
function initGame() {
    return __awaiter(this, void 0, void 0, function* () {
        log("Init game...");
        yield processCommand(InputCommand.INIT);
        yield initPlayers();
        sendCommand(OutputCommand.END_INIT);
    });
}
function initPlayers() {
    return __awaiter(this, void 0, void 0, function* () {
        log("Init players...");
        sendCommand(OutputCommand.GET_PLAYERS);
        return processCommand(InputCommand.GET_PLAYERS, initPlayer);
    });
}
/**
 * Sets local state for the players and sends the player id to the player
 * @param nickname The nickname for the player assigned by the game manager
 * @param index The index of the player assigned by the game manager
 */
function initPlayer(nickname, index) {
    players[index] = nickname;
    // TODO: Investigate why index is 0 based but id is 1 based.
    const playerId = index + 1;
    sendCommand(OutputCommand.SEND_PLAYER_INPUT, [index, playerId]);
    return true;
}
function doGameTurn(turn) {
    return __awaiter(this, void 0, void 0, function* () {
        log('Start turn: ' + turn);
        const playerIndex = turn % players.length;
        // Send inputs
        for (let l = 0; l < 3; l++) {
            const gridString = `${grid[l][0]} ${grid[l][1]} ${grid[l][2]}`;
            sendCommand(OutputCommand.SEND_PLAYER_INPUT, [playerIndex, gridString]);
        }
        sendCommand(OutputCommand.EXECUTE_PLAYER, playerIndex);
        sendCommand(OutputCommand.GET_PLAYER_OUTPUTS, playerIndex);
        yield processCommand(InputCommand.GET_PLAYER_OUTPUTS, (line, index) => __awaiter(this, void 0, void 0, function* () {
            console.log(`Player output line ${index}: ${line}`);
            const output = line.trim().split(' ');
            let targetRow, targetCol;
            try {
                targetRow = parseInt(output[0], 10);
                targetCol = parseInt(output[1], 10);
            }
            catch (_a) {
                const message = 'Wrong output!';
                declareLoser(playerIndex, message);
                return false;
            }
            if (targetRow < 0 || targetRow >= 3 || targetCol < 0 || targetCol >= 3 || grid[targetRow][targetCol] != 0) {
                declareLoser(playerIndex, "Invalid action.");
            }
            const message = `Player ${players[playerIndex]} played (${targetRow} ${targetCol})`;
            sendCommand(OutputCommand.ADD_SUMMARY, message);
            // update grid
            grid[targetRow][targetCol] = playerIndex + 1;
            if (checkWinner() > 0) {
                declareWinner(playerIndex);
            }
            sendCommand(OutputCommand.END_TURN);
            return true;
        }));
    });
}
function declareLoser(playerIndex, message) {
    sendCommand(OutputCommand.DEACTIVATE_PLAYER, [playerIndex, message]);
    sendCommand(OutputCommand.SET_PLAYER_SCORE, [playerIndex, -1]);
    endGame();
}
function checkWinner() {
    for (let i = 0; i < 3; i++) {
        // check rows
        if (grid[i][0] > 0 && grid[i][0] == grid[i][1] && grid[i][0] == grid[i][2]) {
            return grid[i][0];
        }
        // check cols
        if (grid[0][i] > 0 && grid[0][i] == grid[1][i] && grid[0][i] == grid[2][i]) {
            return grid[0][i];
        }
    }
    // check diags
    if (grid[0][0] > 0 && grid[0][0] == grid[1][1] && grid[0][0] == grid[2][2]) {
        return grid[0][0];
    }
    if (grid[2][0] > 0 && grid[2][0] == grid[1][1] && grid[2][0] == grid[0][2]) {
        return grid[2][0];
    }
    return 0;
}
function declareWinner(playerIndex) {
    sendCommand(OutputCommand.ADD_SUMMARY_SUCCESS, [playerIndex, `${players[playerIndex]} Won!`]);
    sendCommand(OutputCommand.SET_PLAYER_SCORE, [playerIndex, 1]);
    endGame();
}
function endGame() {
    sendCommand(OutputCommand.END_GAME);
    gameOver = true;
}
function isNullOrEmpty(value) {
    if (value === null || value === undefined || value === '') {
        return true;
    }
    return false;
}
