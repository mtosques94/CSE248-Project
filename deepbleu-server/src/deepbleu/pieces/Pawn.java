package deepbleu.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import deepbleu.Board;
import deepbleu.Piece;
import deepbleu.Player;

/**
 * @author Matthew Tosques
 */
public class Pawn extends Piece {

    public Pawn(int x, int y, Player p) {
        super(x, y, p);
    }
    
    //copy constructor
    public Pawn(Pawn p) {
        super(p);
    }
    
    @Override
    public char getChar() {
        if (this.player.isWhite)
            return 'P';
        return 'p';
    }
        
    @Override
    public boolean canMoveToLocation(int x, int y, Board b, boolean ignoreTurns) {
        if (super.canMoveToLocation(x, y, b, ignoreTurns)) { //basics
            boolean enemyThere = b.hasEnemyAt(x, y, this);
            int deltaX = x - this.x;  int deltaY = y - this.y;
            if(deltaY == 0) {
                if((deltaX == -1 && this.player.isWhite) || (deltaX == 1 && !this.player.isWhite)) 
                    return !enemyThere;  //pawn can move one space forward if nobody is there
                if(Math.abs(deltaX) == 2 && this.moveCount == 0) //pawn can move two spaces forward on first move
                    return !enemyThere && b.isValidPath(this.computePath(this.x, this.y, x, y)); //no hops
            }
            else if((Math.abs(deltaY) == 1 && deltaX == -1 && this.player.isWhite)
                    || (Math.abs(deltaY) == 1 && deltaX == 1 && !this.player.isWhite)) 
                return enemyThere; //pawns can move 1 tile diagonally if this captures an enemy
        }
        return false;
    }

    @Override
    public int[][] computePath(int startX, int startY, int finalX, int finalY) {
        ArrayList<int[]> path = new ArrayList();
        path.add(new int[] {startX,startY});  //always need start
        if(finalX - startX == -2) 
            path.add(new int[] {--startX,startY});  //middle point of two tile first move for white    
        if(finalX - startX == 2) 
            path.add(new int[] {++startX,startY});  //middle point of two tile first move for black
        path.add(new int[] {finalX,finalY});  //always need end
        int[][] finalPath = new int[path.size()][2];
        int i = 0;
        for (int[] coords : path) 
            finalPath[i++] = coords;
        return finalPath;
    }

    @Override
    public Collection<int[]> getLegalMoves(Board b) {
        HashSet<int[]> legalMoves = new HashSet();
        if(this.player.isWhite){
            if (this.canMoveToLocation(this.x-1, this.y, b, true))
                legalMoves.add(new int[]{this.x-1, this.y});
            if (this.canMoveToLocation(this.x-2, this.y,b, true))
                legalMoves.add(new int[]{this.x-2, this.y});  
            if (this.canMoveToLocation(this.x-1, this.y-1,b, true))
                legalMoves.add(new int[]{this.x-1, this.y-1});
            if (this.canMoveToLocation(this.x-1, this.y+1,b, true))
                legalMoves.add(new int[]{this.x-1, this.y+1});
        }
        else {
            if (this.canMoveToLocation(this.x+1, this.y,b, true))
                legalMoves.add(new int[]{this.x+1, this.y});        
            if (this.canMoveToLocation(this.x+2, this.y,b, true))
                legalMoves.add(new int[]{this.x+2, this.y});  
            if (this.canMoveToLocation(this.x+1, this.y-1, b,true))
                legalMoves.add(new int[]{this.x+1, this.y-1});
            if (this.canMoveToLocation(this.x+1, this.y+1,b, true))
                legalMoves.add(new int[]{this.x+1, this.y+1});
        }
        return legalMoves;
    }
}