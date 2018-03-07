const myId = parseInt(readline(), 10);
const grid = [[0,0,0],[0,0,0],[0,0,0]];

while (true) {
    updateBoard();
    print(getMove());
}

function getMove() {
    for (let r = 0; r < 3; r++) {
        for (let c = 0; c < 3; c++) {
            if (grid[r][c] === 0) {
                return r + " " + c;
            }
        }
    }
}

function updateBoard() {
    for (let r = 0; r < 3; r++) {
        let row = readline().split(' ');
        for (let c = 0; c < 3; c++) {
            grid[r][c] = parseInt(row[c], 10);
        }
    }
}

function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}