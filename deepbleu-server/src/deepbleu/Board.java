package deepbleu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import deepbleu.pieces.Bishop;
import deepbleu.pieces.King;
import deepbleu.pieces.Knight;
import deepbleu.pieces.Pawn;
import deepbleu.pieces.Queen;
import deepbleu.pieces.Rook;

/**       
 * Represents a chess board with two players.
 * Keeps track of previous moves/captures so you (or the AI) can rewind the game.
 *
 * @author Matthew Tosques
 */
public class Board {
    
    //Toggle whether players are forced to exit check and if board prevents self inflicted check for ALL moves.
    //Otherwise self inflicted check is only prevented when moving one's King.
    private static boolean AI_EXIT_CHECK = false; //NOT RECOMMENDED, code is way too slow. 
                                                  //AI doesn't need to be forced out anyway.
    private static boolean GUI_EXIT_CHECK = true; //Restricting GUI moves works fine.
    
    Piece[][] tiles = new Piece[8][8]; //A 2D array will provide modestly efficient lookup.
    ArrayList<ChessMove> moveHistory = new ArrayList(); //Every canon move since the game began.
    final char[][] simpleBoard = new char[8][8]; //A text-only view of the board.
    
    Player player1, player2, currentPlayer; //The two players, and a reference indicating who's turn it is.
    Piece selected = null; //The most recent valid piece clicked by a GUIPlayer.
    
    /**
     * Board constructor.  Takes two players.
     * Arranges pieces for a new game and configures the GUI.
     * Either player can be white.  
     * @param p1 Player one.
     * @param p2 Player two.
     */
    public Board(Player p1, Player p2) {
        //initLighting();
    	this.player1 = p1;
        this.player2 = p2;
        Player white;
        Player black;

        // White goes first.
        if (player1.isWhite) {
            currentPlayer = player1;
            white = player1;
            black = player2;
        } else {
            currentPlayer = player2;
            white = player2;
            black = player1;
        }

        //Using an ArrayList to make this a bit more readable.
        ArrayList<Piece> inPlay = new ArrayList(32);
        //Add white pieces to the board.
        for (int x = 0; x < 8; x++) {
            inPlay.add(new Pawn(6, x, white));
        }
        inPlay.add(new Rook(7, 0, white));
        inPlay.add(new Knight(7, 1, white));
        inPlay.add(new Bishop(7, 2, white));
        inPlay.add(new Queen(7, 3, white));
        inPlay.add(new King(7, 4, white));
        inPlay.add(new Bishop(7, 5, white));
        inPlay.add(new Knight(7, 6, white));
        inPlay.add(new Rook(7, 7, white));
        //Add black pieces to the board.
        for (int x = 0; x < 8; x++) {
            inPlay.add(new Pawn(1, x, black));
        }
        inPlay.add(new Rook(0, 0, black));
        inPlay.add(new Knight(0, 1, black));
        inPlay.add(new Bishop(0, 2, black));
        inPlay.add(new Queen(0, 3, black));
        inPlay.add(new King(0, 4, black));
        inPlay.add(new Bishop(0, 5, black));
        inPlay.add(new Knight(0, 6, black));
        inPlay.add(new Rook(0, 7, black));
        //Convert ArrayList to 2d array of pieces.
        inPlay.forEach((p) -> {tiles[p.x][p.y] = p;});
    }

    /**
     * Deep-copy constructor
     * Needed for concurrent AI
     * @param b the board to clone
     */
    public Board(Board b) {
        this.player1 = b.player1;
        this.player2 = b.player2;
        this.currentPlayer = b.currentPlayer;
        //Copy every piece.
        for (int i = 0; i < b.tiles.length; i++) {
            for (int j = 0; j < b.tiles[i].length; j++) {
                Piece p = b.tiles[i][j];
                if (p == null)
                    this.tiles[i][j] = null;
                else {
                    if (p instanceof Rook) this.tiles[i][j] = new Rook((Rook) p);
                    if (p instanceof Bishop) this.tiles[i][j] = new Bishop((Bishop) p);
                    if (p instanceof Queen) this.tiles[i][j] = new Queen((Queen) p);
                    if (p instanceof King) this.tiles[i][j] = new King((King) p);
                    if (p instanceof Pawn) this.tiles[i][j] = new Pawn((Pawn) p);
                    if (p instanceof Knight) this.tiles[i][j] = new Knight((Knight) p);
                }
            }
        }
        //copy moveHistory
        b.moveHistory.forEach((m) -> {this.moveHistory.add(new ChessMove(m));});
    }
    
    /**
     * Counts all pieces in play.
     * An AITask increments its search depth by 1 after half of all pieces have been captured.
     * @return number of pieces currently on the board
     */
    public int numPiecesInPlay() {
        int num = 0;
        for(Piece[] row : tiles)
            for(Piece p : row) 
                if(p != null)
                    num++;
        return num;
    }

    /**
     * Switches the current player.    
     */
    void switchTurns() {
        if (currentPlayer == player1) 
            currentPlayer = player2;
        else currentPlayer = player1;
    }

    /**
     * Moves a piece.    
     * CAREFUL: The move method does not check for important things like hopping or piece ownership.
     * @param move A ChessMove that WILL happen.
     */
    public void move(ChessMove move) {
        //If this move captures an enemy keep a reference to it in the move object.
        if (tiles[move.toRow][move.toCol] != null) 
            move.enemyCaptured = tiles[move.toRow][move.toCol];
        //Find the piece in question and move it.
        tiles[move.toRow][move.toCol] = tiles[move.fromRow][move.fromCol];
        tiles[move.toRow][move.toCol].x = move.toRow;
        tiles[move.toRow][move.toCol].y = move.toCol;
        tiles[move.toRow][move.toCol].moveCount++;
        tiles[move.fromRow][move.fromCol] = null;
        moveHistory.add(move);
        switchTurns();
    }

    /**
     * Undoes the previous move.
     * The AI needs to undo moves to traverse game tree.
     */
    public void undoLastMove() {
        ChessMove lastMove = this.moveHistory.get(this.moveHistory.size() - 1);
        tiles[lastMove.fromRow][lastMove.fromCol] = tiles[lastMove.toRow][lastMove.toCol];
        tiles[lastMove.fromRow][lastMove.fromCol].moveCount--;
        tiles[lastMove.fromRow][lastMove.fromCol].x = lastMove.fromRow;
        tiles[lastMove.fromRow][lastMove.fromCol].y = lastMove.fromCol;
        if (lastMove.enemyCaptured != null) 
            tiles[lastMove.toRow][lastMove.toCol] = lastMove.enemyCaptured;
        else tiles[lastMove.toRow][lastMove.toCol] = null;
        moveHistory.remove(this.moveHistory.size() - 1);
        switchTurns();
    }

    /**
     * Can we really do that?  (This method is only called for a ConsolePlayer. See main for proof.)
     * @param move A ChessMove that may or may not be legal.
     * @return validity of given move
     */
    public boolean isLegalMove(ChessMove move) {
        //you can't move an empty tile
        if(tiles[move.fromRow][move.fromCol] == null)
            return false;
        //you can't capture your own piece
        if(tiles[move.toRow][move.toCol] != null)
            if(tiles[move.toRow][move.toCol].player == this.currentPlayer)
                return false;
        //otherwise just follow the rules for that type of piece
        return tiles[move.fromRow][move.fromCol].canMoveToLocation(move.toRow, move.toCol, this);
    }
    
    /**
     * Makes sure a piece doesn't hop over any other piece(s) during a move.
     * This method is called in the canMoveToLocation method of a each piece.
     * Knight does not need this functionality.
     * Does not check starting tile or destination tile, only the path between.
     * @param path Array of integers ostensibly representing x and y values of tiles to hop over.
     * @return validity of path
     */
    public boolean isValidPath(int[][] path) {
        for (int x = 1; x < path.length - 1; x++) 
            if (!isEmpty(path[x][0], path[x][1])) 
                return false;
        return true;
    }

    /**
     * Checks that a tile is empty
     * @param x row
     * @param y column
     * @return true if tile is empty
     */
    public boolean isEmpty(int x, int y) {
        return tiles[x][y] == null;
    }

    /**
     * Checks for an enemy of a given piece on a given tile.
     * @param x row
     * @param y column
     * @param p Piece that might have enemies.
     * @return true if the given piece has an enemy at given location
     */
    public boolean hasEnemyAt(int x, int y, Piece p) {
        return tiles[x][y] != null && tiles[x][y].player != p.player;
    }

    /**
     * Checks for a friend of a given piece on a given tile.
     * @param x row
     * @param y column
     * @param p Piece that might have friends.
     * @return true if the given piece has a friend at given location
     */
    public boolean hasFriendlyAt(int x, int y, Piece p) {
        return tiles[x][y] != null && tiles[x][y].player == p.player;
    }

    /**
     * Can the enemy of a given piece move to a given location?
     * A player mustn't put their own king in check.
     * @param x row
     * @param y column
     * @param p the piece you are concerned about
     * @return true if the enemy can capture the given piece 
     */
    public boolean enemyCanMoveToLocation(int x, int y, Piece p) {
        for (Piece[] row : tiles) 
            for (Piece tile : row) 
                if (tile != null && tile.player != p.player && !(tile instanceof King))
                    for (int[] move : tile.getLegalMoves(this))                             
                        if (move[0] == x && move[1] == y)
                            return !(tile instanceof Pawn && Math.abs(move[1]-tile.y) == 0);
        return false;
    }

    /**
     * Computes every legal move for current player.
     * @param checkForCheck whether to prevent self inflicted check / force player out of check
     * @return Collection of every legal move for current player.
     */
    public Collection<ChessMove> getAllLegalMoves(boolean checkForCheck) {
        HashSet<ChessMove> allLegalMoves = new HashSet<>(83);
        for (Piece[] row : tiles) 
            for (Piece p : row) 
                if (p != null && p.belongsToCurrentPlayer(this)) 
                    for (int[] move : p.getLegalMoves(this)) {
                        ChessMove chessMove = new ChessMove(p.x, p.y, move[0], move[1]);
                        if(!AI_EXIT_CHECK || !checkForCheck || !this.causesCheck(chessMove)) {
                            allLegalMoves.add(chessMove);
                        }
                    }
        return allLegalMoves;
    }
    
    /**
     * Convenience method: by default respect rules of check
     * @return Collection of every legal move for current player.
     */
    public Collection<ChessMove> getAllLegalMoves() {
        return this.getAllLegalMoves(true);
    }
    
    /**
     * Determines if a move results in self inflicted check.
     * @param move A ChessMove that may or may not break the rules of check
     * @return legality of given move with regard to check
     */
    boolean causesCheck(ChessMove move) {
        boolean ret = false;
        this.move(move);
        this.switchTurns();
        if (this.hasCheck()) {
            ret = true;
        }
        this.switchTurns();
        this.undoLastMove();
        return ret;
    }

    /**
     * Check if the current board forces a draw.
     * For now, the only draw condition is King vs King with no other pieces.
     * @return true if the game is a draw
     */
    public boolean hasDraw() {
        int numKings = 0;
        int totalPieces = 0;
        for (Piece[] row : tiles) 
            for (Piece p : row) 
                if (p != null) {
                    if (p instanceof King) 
                        numKings++;
                    totalPieces++;
                }
        if (numKings == 2)
            return totalPieces == 2;
        return false;
    }

    /**
     * Gets the winning player.  Don't call this until there is one.
     * GameOfChess (main) will call this method once it has detected endgame.
     * @return the winner or some dummy player representing a draw
     */
    public Player getWinner() {
        if (this.kingCaptured() || this.hasCheck() || this.getAllLegalMoves().isEmpty()) {
            switchTurns();
            Player winner = currentPlayer;
            switchTurns();
            return winner;
        }
        return new ConsolePlayer("DRAW", false);
    }

    /**
     * Has either king been captured?
     * This is how the AI realizes a path to a checkmate.
     * @return true if either king was already captured
     */
    public boolean kingCaptured() {
        for(ChessMove move : moveHistory)
            if(move.enemyCaptured != null && move.enemyCaptured instanceof King)
                return true;
        return false;
    }
    
    /**
     * Is the current player's king in check?
     * @return true if current player's king is in check
     */
    public boolean hasCheck() {
        //find current player's king
        Piece king = null;
        for (Piece[] row : tiles) 
            for (Piece p : row) 
                if (p != null) 
                    if (p instanceof King && p.player == this.currentPlayer) 
                        king = p;
        //check if the enemy can capture him
        switchTurns();
        //Without setting checkForCheck to false, this line would create an infinite loop.
        for(ChessMove move : this.getAllLegalMoves(false))
            if(move.toRow == king.x && move.toCol == king.y){
                switchTurns();
                return true;
            }
        switchTurns();
        return false;
    }

    /**
     * Updates the character-based representation of the board in memory.
     */
    public void updateSimpleBoard() {
        for (int x = 0; x < tiles.length; x++) 
            for (int y = 0; y < tiles[x].length; y++) {
                if (tiles[x][y] == null)
                    simpleBoard[x][y] = ' ';
                else 
                    simpleBoard[x][y] = tiles[x][y].getChar();
            }
    }

    /**
     * Returns board as text.
     * @return the current board as text with labels for x and y axis.
     */
    @Override
    public String toString() {
        updateSimpleBoard();
        StringBuilder sb = new StringBuilder();
        sb.append("     [a, b, c, d, e, f, g, h]\n\n");
        int rowNum = 0;
        for (char[] row : simpleBoard) 
            sb.append(8 - (rowNum++)).append("    ").append(Arrays.toString(row)).append("\n");
        return sb.toString();
    }
    
    /**
     * Generate a SaveState of the current board.
     * Be sure to write to a file immediately, fields are shallow-copied.
     * @return SaveState of this board.
     */
    public SaveState getSaveState() {
        return new SaveState(this);
    }
    
    /**
     * Load a save state.
     * @param save the SaveState to load
     */
    public void loadSaveState(SaveState save) {
        this.selected = null;
        this.tiles = save.getTiles();
        this.moveHistory = save.getMoveHistory();
        this.player1 = save.getPlayerOne();
        this.player2 = save.getPlayerTwo();
        this.currentPlayer = save.getCurrentPlayer();
    }
    
    public void setPlayerOne(Player newPlayer) {
        for(Piece[] row : tiles)
            for(Piece p : row) 
                if(p != null && p.player == this.player1)
                    p.player = newPlayer;
        this.player1 = newPlayer;
    }
    
    public void setPlayerTwo(Player newPlayer) {
        for(Piece[] row : tiles)
            for(Piece p : row) 
                if(p != null && p.player == this.player2)
                    p.player = newPlayer;
        this.player2 = newPlayer;
    }
    
}