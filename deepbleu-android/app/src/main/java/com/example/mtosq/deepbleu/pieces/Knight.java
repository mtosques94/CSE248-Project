package com.example.mtosq.deepbleu.pieces;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.example.mtosq.deepbleu.Board;
import com.example.mtosq.deepbleu.Piece;
import com.example.mtosq.deepbleu.Player;
import com.example.mtosq.deepbleu.R;

import java.util.HashSet;

/**
 * @author Matthew Tosques
 */
public class Knight extends Piece {

    private static final int DEFAULT_WHITE = R.drawable.whiteknight;
    private static final int DEFAULT_BLACK = R.drawable.blackknight;

    public Knight(int x, int y, Player p) {
        super(x, y, p);
    }

    //copy constructor
    public Knight(Knight p) {
        super(p);
    }

    @Override
    public char getChar() {
        if (this.player.isWhite)
            return 'N';
        return 'n';
    }

    @Override
    public int getDefaultImage() {
        if(this.player.isWhite)
            return DEFAULT_WHITE;
        return DEFAULT_BLACK;
    }

    @Override
    public boolean canMoveToLocation(int x, int y, Board b, boolean ignoreTurns) {
        if (super.canMoveToLocation(x, y, b, ignoreTurns)) { //basics
            int deltaX = x - this.x;
            int deltaY = y - this.y;
            //Any 2x1 L-shaped path.  Hops are fine.
            return  (( (deltaX == -2 && deltaY == -1)
                    || (deltaX == -2 && deltaY ==  1)
                    || (deltaX ==  2 && deltaY == -1)
                    || (deltaX ==  2 && deltaY ==  1)
                    || (deltaX == -1 && deltaY == -2)
                    || (deltaX ==  1 && deltaY == -2)
                    || (deltaX == -1 && deltaY ==  2)
                    || (deltaX ==  1 && deltaY ==  2)));
        }
        return false;
    }

    //Knights can hop, I don't think this will be needed.
    @Override
    public int[][] computePath(int startX, int startY, int finalX, int finalY) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public HashSet<int[]> getLegalMoves(Board b) {
        HashSet<int[]> legalMoves = new HashSet();
        if (this.canMoveToLocation(this.x-2, this.y-1,b,  true))
            legalMoves.add(new int[]{this.x-2, this.y-1});
        if (this.canMoveToLocation(this.x-2, this.y+1,b, true))
            legalMoves.add(new int[]{this.x-2, this.y+1});
        if (this.canMoveToLocation(this.x+2, this.y-1,b, true))
            legalMoves.add(new int[]{this.x+2, this.y-1});
        if (this.canMoveToLocation(this.x+2, this.y+1,b, true))
            legalMoves.add(new int[]{this.x+2, this.y+1});
        if (this.canMoveToLocation(this.x-1, this.y-2,b, true))
            legalMoves.add(new int[]{this.x-1, this.y-2});
        if (this.canMoveToLocation(this.x+1, this.y-2,b, true))
            legalMoves.add(new int[]{this.x+1, this.y-2});
        if (this.canMoveToLocation(this.x-1, this.y+2,b, true))
            legalMoves.add(new int[]{this.x-1, this.y+2});
        if (this.canMoveToLocation(this.x+1, this.y+2,b, true))
            legalMoves.add(new int[]{this.x+1, this.y+2});
        return legalMoves;
    }
}