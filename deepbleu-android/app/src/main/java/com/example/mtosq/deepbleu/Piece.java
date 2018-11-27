package com.example.mtosq.deepbleu;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.HashSet;

public abstract class Piece implements Serializable {

    public int x, y;
    public Player player;
    public int moveCount = 0;

    public Piece(int x, int y, Player p) {
        this.x = x;
        this.y = y;
        this.player = p;
    }

    /**
     * Deep-copy constructor
     * @param p the piece to clone
     */
    public Piece(Piece p) {
        this.x = p.x;
        this.y = p.y;
        this.player = p.player;
    }

    /**
     * Does this piece belong to the current player?
     * @return true if the current player owns this piece
     */
    public boolean belongsToCurrentPlayer(Board b) {
        return this.player == b.currentPlayer;
    }

    /**
     * Basic logic about moving a piece, has nothing to do with any specific piece.
     * 1. This must be your piece.
     * 2. Destination tile must be somewhere on the board.
     * 3. You mustn't have a piece of your own on the destination tile.
     * 4. (Effectively implied by 3) No zero distance moves.
     * @param x potential destination row
     * @param y potential destination column
     * @param ignoreTurns true if we should ignore currentPlayer
     * @return true if the move is valid
     */
    public boolean canMoveToLocation(int x, int y, Board b, boolean ignoreTurns) {
        return (this.belongsToCurrentPlayer(b) || ignoreTurns)
                && !(x < 0 || y < 0 || x > 7 || y > 7 || b.hasFriendlyAt(x, y, this));
    }

    /**
     * Convenience method: by default respect turns
     * @param x potential destination row
     * @param y potential destination column
     * @return true if the move is valid
     */
    public boolean canMoveToLocation(int x, int y, Board b) {
        return this.canMoveToLocation(x, y, b, false);
    }

    /**
     * For a given legal destination, return the x and y values of any tiles hopped.
     * @param startX source row
     * @param startY source column
     * @param finalX destination row
     * @param finalY destination column
     * @return x and y values of any tiles hopped stored as 2d array
     */
    public abstract int[][] computePath(int startX, int startY, int finalX, int finalY);

    /**
     * Return every legal move for this piece.
     * @return HashSet of every legal destination for this piece
     */
    public abstract HashSet<int[]> getLegalMoves(Board b);

    /**
     * Return an image of this piece.
     * @return a JavaFX Image object representing this piece
     */
    public abstract Drawable getDefaultImage();

    /**
     * A single character representing this piece.
     * @return a character representing this piece
     */
    public abstract char getChar();
}
