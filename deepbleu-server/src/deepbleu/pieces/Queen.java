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
public class Queen extends Piece {
    
    static final int[][] ALL_DELTAS = new int[][] {{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1}};
    
    public Queen(int x, int y, Player p) {
        super(x, y, p);
    }
    
    //copy constructor
    public Queen(Queen p) {
        super(p);
    }
    
    @Override
    public char getChar() {
        if (this.player.isWhite)
            return 'Q';
        return 'q';
    }
        
    @Override
    public boolean canMoveToLocation(int x, int y, Board b, boolean ignoreTurns) {
        if (super.canMoveToLocation(x, y, b, ignoreTurns)) { //basics
            int deltaX = x - this.x;
            int deltaY = y - this.y;   
            if  (( (deltaX  < 0 && deltaY  > 0 && Math.abs(deltaX) == Math.abs(deltaY)) //any diagonal path
                || (deltaX  > 0 && deltaY  > 0 && Math.abs(deltaX) == Math.abs(deltaY)) 
                || (deltaX  > 0 && deltaY  < 0 && Math.abs(deltaX) == Math.abs(deltaY))
                || (deltaX  < 0 && deltaY  < 0 && Math.abs(deltaX) == Math.abs(deltaY))
                || (deltaX  < 0 && deltaY == 0) //or any straight path
                || (deltaX == 0 && deltaY  < 0)    
                || (deltaX  > 0 && deltaY == 0)
                || (deltaX == 0 && deltaY  > 0)))
                    return b.isValidPath(this.computePath(this.x, this.y, x, y)); //no hops
        }
        return false;
    }

    @Override
    public int[][] computePath(int startX, int startY, int finalX, int finalY) {
        ArrayList<int[]> path = new ArrayList();
        if (finalX < startX) 
            if (finalY < startY) 
                while (startX != finalX)   path.add(new int[]{startX--, startY--});  
            else if (finalY > startY) 
                while (startX != finalX)   path.add(new int[]{startX--, startY++});      
            else while (startX != finalX)  path.add(new int[]{startX--, startY});
         else if (finalX > startX) 
            if (finalY < startY)
                while (startX != finalX)   path.add(new int[]{startX++, startY--});
            else if (finalY > startY) 
                while (startX != finalX)   path.add(new int[]{startX++, startY++}); 
            else while (startX != finalX)  path.add(new int[]{startX++, startY});
         else if (finalY < startY) 
                while (startY != finalY)   path.add(new int[]{startX, startY--}); 
            else while (startY != finalY)  path.add(new int[]{startX, startY++});
        path.add(new int[]{finalX, finalY});
        int[][] finalPath = new int[path.size()][2];
        int i = 0;
        for (int[] coords : path) 
            finalPath[i++] = coords;
        return finalPath;
    }

    @Override
    public Collection<int[]> getLegalMoves(Board b) {
        HashSet<int[]> legalMoves = new HashSet();        
        int deltaX, deltaY; int _deltaX, _deltaY;
        for(int[] delta : ALL_DELTAS) {
            deltaX = delta[0];
            _deltaX = deltaX;
            deltaY = delta[1];
            _deltaY = deltaY;
            while(this.canMoveToLocation(x+deltaX, y+deltaY, b)) {
                legalMoves.add(new int[] {x+deltaX, y+deltaY});
                deltaX += _deltaX;
                deltaY += _deltaY;
            }
        }
        return legalMoves;
    }
}