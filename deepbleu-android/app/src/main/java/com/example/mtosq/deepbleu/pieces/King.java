package com.example.mtosq.deepbleu.pieces;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.example.mtosq.deepbleu.Board;
import com.example.mtosq.deepbleu.ChessMove;
import com.example.mtosq.deepbleu.Piece;
import com.example.mtosq.deepbleu.Player;
import com.example.mtosq.deepbleu.R;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Matthew Tosques
 */
public class King extends Piece {

    static final Drawable DEFAULT_WHITE = null;
    static final Drawable DEFAULT_BLACK = null;

    public King(int x, int y, Player p) {
        super(x, y, p);
    }

    //copy constructor
    public King(King p) {
        super(p);
    }

    @Override
    public char getChar() {
        if (this.player.isWhite)
            return 'K';
        return 'k';
    }

    @Override
    public Drawable getDefaultImage() {
        if(this.player.isWhite)
            return DEFAULT_WHITE;
        return DEFAULT_BLACK;
    }

    @Override
    public boolean canMoveToLocation(int x, int y, Board b, boolean ignoreTurns) {
        if (super.canMoveToLocation(x, y, b, ignoreTurns)) { //basics
            int deltaX = x - this.x;  int deltaY = y - this.y;
            //one tile in any direction
            if  (( (deltaX == -1 && deltaY ==  0)
                    || (deltaX == -1 && deltaY ==  1)
                    || (deltaX ==  0 && deltaY ==  1)
                    || (deltaX ==  1 && deltaY ==  1)
                    || (deltaX ==  1 && deltaY ==  0)
                    || (deltaX ==  1 && deltaY == -1)
                    || (deltaX ==  0 && deltaY == -1)
                    || (deltaX == -1 && deltaY == -1))) {
                //you can't put your own king in check
                boolean selfInflictedCheck = false;
                b.move(new ChessMove(this.x, this.y, x, y));
                if (b.enemyCanMoveToLocation(this.x, this.y, this))
                    selfInflictedCheck = true;
                b.undoLastMove();
                return !selfInflictedCheck;
            }
        }
        return false;
    }

    @Override
    public int[][] computePath(int startX, int startY, int finalX, int finalY) {
        ArrayList<int[]> path = new ArrayList();
        path.add(new int[]{startX, startY});
        path.add(new int[]{finalX, finalY});
        int[][] finalPath = new int[path.size()][2];
        int i = 0;
        for (int[] coords : path)
            finalPath[i++] = coords;
        return finalPath;
    }

    @Override
    public HashSet<int[]> getLegalMoves(Board b) {
        HashSet<int[]> legalMoves = new HashSet();
        if (this.canMoveToLocation(this.x-1, this.y-1,b, true))
            legalMoves.add(new int[] {this.x-1,this.y-1});
        if (this.canMoveToLocation(this.x-1, this.y,b, true))
            legalMoves.add(new int[] {this.x-1,this.y});
        if (this.canMoveToLocation(this.x-1, this.y+1,b, true))
            legalMoves.add(new int[] {this.x-1,this.y+1});
        if (this.canMoveToLocation(this.x, this.y-1,b, true))
            legalMoves.add(new int[] {this.x,this.y-1});
        if (this.canMoveToLocation(this.x, this.y+1,b, true))
            legalMoves.add(new int[] {this.x,this.y+1});
        if (this.canMoveToLocation(this.x+1, this.y-1, b,true))
            legalMoves.add(new int[] {this.x+1,this.y-1});
        if (this.canMoveToLocation(this.x+1, this.y, b,true))
            legalMoves.add(new int[] {this.x+1,this.y});
        if (this.canMoveToLocation(this.x+1, this.y+1,b, true))
            legalMoves.add(new int[] {this.x+1,this.y+1});
        return legalMoves;
    }
}