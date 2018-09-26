package deepbleu;

import deepbleu.pieces.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javafx.scene.Node;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/**       
 * Represents a chess board with two players.
 * Keeps track of previous moves/captures so you (or the AI) can rewind the game.
 *
 * @author Matthew Tosques
 */
public class Board extends GridPane {
    
    //Toggle GUI response
    private boolean enabled = true;
    
    //Toggle whether players are forced to exit check and if board prevents self inflicted check for ALL moves.
    //Otherwise self inflicted check is only prevented when moving one's King.
    private static boolean AI_EXIT_CHECK = false; //NOT RECOMMENDED, code is way too slow. 
                                                  //AI doesn't need to be forced out anyway.
    private static boolean GUI_EXIT_CHECK = true; //Restricting GUI moves works fine.
    
    //Background tile images
    private static final Image TILE_LIGHT = new Image("/img/small/PureWhite.png"); //Image for light tile
    private static final Image TILE_DARK = new Image("/img/small/PureGrey.png"); //Image for dark tile
    //Transparent image, sits on top of tile if there is no piece
    private static final Image TILE_BLANK = new Image("img/small/Transparent.png"); 
    //Lighting for hightlighting tiles & pieces
    private static final Lighting DEFAULT_LIGHTING = new Lighting(); 
    private static final Lighting PICKUP_LIGHTING = new Lighting();
    private static final Lighting LEGALMOVE_LIGHTING = new Lighting();
    private static final Lighting CHECK_LIGHTING = new Lighting();
    private static final Color PICKUP_COLOR = Color.CYAN;
    private static final Color LEGALMOVE_COLOR = Color.GREEN;
    private static final Color CHECK_COLOR = Color.RED;
    
    Piece[][] tiles = new Piece[8][8]; //A 2D array will provide modestly efficient lookup.
    ArrayList<ChessMove> moveHistory = new ArrayList(); //Every canon move since the game began.
    final char[][] simpleBoard = new char[8][8]; //A text-only view of the board.
    
    Player player1, player2, currentPlayer; //The two players, and a reference indicating who's turn it is.
    Piece selected = null; //The most recent valid piece clicked by a GUIPlayer.
    
    /**
     * Apply lighting effects.
     */
    private static void initLighting() {
        PICKUP_LIGHTING.setLight(new Light.Distant(45, 45, PICKUP_COLOR));
        PICKUP_LIGHTING.setSurfaceScale(2);
        PICKUP_LIGHTING.setDiffuseConstant(1);
        LEGALMOVE_LIGHTING.setLight(new Light.Distant(45, 45, LEGALMOVE_COLOR));
        LEGALMOVE_LIGHTING.setSurfaceScale(2);
        LEGALMOVE_LIGHTING.setDiffuseConstant(1);
        CHECK_LIGHTING.setLight(new Light.Distant(45, 45, CHECK_COLOR));
        CHECK_LIGHTING.setSurfaceScale(2);
        CHECK_LIGHTING.setDiffuseConstant(1);
    }

    /**
     * Board constructor.  Takes two players.
     * Arranges pieces for a new game and configures the GUI.
     * Either player can be white.  
     * @param p1 Player one.
     * @param p2 Player two.
     */
    public Board(Player p1, Player p2) {
        initLighting();
        this.player1 = p1;
        this.player2 = p2;
        //White goes first.
        if (player1.isWhite) 
            currentPlayer = player1;
        else currentPlayer = player2;
        //Using an ArrayList to make this a bit more readable.
        ArrayList<Piece> inPlay = new ArrayList(32);
        //Add player1's pieces to the board.
        for (int x = 0; x < 8; x++)
            inPlay.add(new Pawn(6, x, p1));
        inPlay.add(new Rook(7, 0, p1));
        inPlay.add(new Knight(7, 1, p1));
        inPlay.add(new Bishop(7, 2, p1));
        inPlay.add(new King(7, 3, p1));
        inPlay.add(new Queen(7, 4, p1));
        inPlay.add(new Bishop(7, 5, p1));
        inPlay.add(new Knight(7, 6, p1));
        inPlay.add(new Rook(7, 7, p1));
        //Add player2's pieces to the board.
        for (int x = 0; x < 8; x++)
            inPlay.add(new Pawn(1, x, p2));
        inPlay.add(new Rook(0, 0, p2));
        inPlay.add(new Knight(0, 1, p2));
        inPlay.add(new Bishop(0, 2, p2));
        inPlay.add(new King(0, 3, p2));
        inPlay.add(new Queen(0, 4, p2));
        inPlay.add(new Bishop(0, 5, p2));
        inPlay.add(new Knight(0, 6, p2));
        inPlay.add(new Rook(0, 7, p2));
        //Convert ArrayList to 2d array of pieces.
        inPlay.forEach((p) -> {tiles[p.x][p.y] = p;});
        //Add background tiles
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (i % 2 == 0) { //even rows start with dark tile
                    if (j % 2 == 0) {
                        this.add(new ImageView(TILE_DARK), i, j);
                    } else {
                        this.add(new ImageView(TILE_LIGHT), i, j);
                    }
                } else { //odd rows start with light tile
                    if (j % 2 != 0) {
                        this.add(new ImageView(TILE_DARK), i, j);
                    } else {
                        this.add(new ImageView(TILE_LIGHT), i, j);
                    }
                }
            }
        }
        //Create and add ImageViews to this board since it's a GridPane.
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                Piece p = tiles[i][j];
                ImageView tileView;
                if(p == null) { //add blank tiles
                    tileView = new ImageView(TILE_BLANK);
                    this.add(tileView, j, i);
                } else { //or load appropriate image of piece
                    tileView = new ImageView(p.getDefaultImage());
                    this.add(tileView, p.y, p.x);
                }
                final ImageView TileView = tileView;
                final int x = i;
                final int y = j;
                //Respond to mouse events
                TileView.setOnMouseClicked(event -> {
                        if (currentPlayer instanceof GUIPlayer && this.enabled) { 
                            if (tiles[x][y] != null
                                    && tiles[x][y].belongsToCurrentPlayer(this)) {//if you click on your own piece
                                this.selected = tiles[x][y]; //you pick it up
                                this.updateGraphics();
                            } else  { // if you click on an empty tile or an enemy
                                if (this.selected != null) { //and you have selected something already
                                    if (this.selected.canMoveToLocation(x, y, this) //if you can move it there
                                            && (!GUI_EXIT_CHECK
                                            || !this.causesCheck(new ChessMove(selected.x,selected.y,x,y)))) { 
                                        System.out.println("Adding move.");
                                        GUIPlayer.PUT_MOVE_HERE.add( //do it
                                                new ChessMove(this.selected.x,this.selected.y,x,y));
                                        this.selected = null; //deselect piece after move
                                    }
                                }
                            }
                        }
                    });
                TileView.setPickOnBounds(true); //ignore png transparency for mouse events
            }
        }
        this.setGridLinesVisible(true); //works for now
        this.updateGraphics(); //apply lighting 
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
     * @return HashSet of every legal move for current player.
     */
    public HashSet<ChessMove> getAllLegalMoves(boolean checkForCheck) {
        HashSet<ChessMove> allLegalMoves = new HashSet();
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
     * @return HashSet of every legal move for current player.
     */
    public HashSet<ChessMove> getAllLegalMoves() {
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
     * Updates the GUI.
     * Call this any time the selected piece changes, and between each move.
     */
    public final void updateGraphics() {
        boolean inCheck = this.hasCheck();
        for (Node node : this.getChildren()) {
            if (node.getClass().getSimpleName().equals("ImageView")) {
                
                ImageView thisTile = (ImageView) node;
                int x = GridPane.getRowIndex(node);
                int y = GridPane.getColumnIndex(node);
                //never change background tiles
                boolean foreground = !(thisTile.getImage().equals(TILE_DARK)
                        || thisTile.getImage().equals(TILE_LIGHT));

                //empty tile
                if (tiles[x][y] == null && foreground) {
                    thisTile.setImage(TILE_BLANK);
                }
                
                //tile with pieces
                else if(tiles[x][y] != null && foreground) {
                    thisTile.setImage(tiles[x][y].getDefaultImage());
                }

                //reset lighting all lighting
                thisTile.setEffect(DEFAULT_LIGHTING);
                
                //apply check lighting
                if(tiles[x][y] != null && inCheck && tiles[x][y].player == this.currentPlayer 
                        && tiles[x][y] instanceof King) {
                    thisTile.setEffect(CHECK_LIGHTING);
                }
                
                //apply selected piece hightlight
                if (this.selected != null
                        && tiles[x][y] == this.selected) {
                    thisTile.setEffect(PICKUP_LIGHTING);
                }
                
                //apply legal move highlight
                if (this.selected != null) {
                    for (int[] destination : this.selected.getLegalMoves(this)) {
                        if (x == destination[0] && y == destination[1] && !this.causesCheck(
                                        new ChessMove(selected.x,selected.y,destination[0],destination[1]))) {
                            thisTile.setEffect(LEGALMOVE_LIGHTING);
                        }
                    }
                }
                
            }
        }
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
    
    /**
     * Enable GUI interaction.
     */
    public void disable() {
        this.enabled = false;
    }
    
    /**
     * Disable GUI interaction.
     */
    public void enable() {
        this.enabled = true;
    }
}
