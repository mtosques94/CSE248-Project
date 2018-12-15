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
public class Bishop extends Piece {
    
    static final int [][] ALL_DELTAS = new int[][] {{-1,1},{1,1},{1,-1},{-1,-1}};
    
    public Bishop(int x, int y, Player p) {
        super(x, y, p);
    }
    
     //copy constructor
    public Bishop(Bishop p) {
        super(p);
    }

    @Override
    public char getChar() {
        if (this.player.isWhite) 
            return 'B';
        return 'b';
    }
        
    @Override
    public boolean canMoveToLocation(int x, int y, Board b, boolean ignoreTurns) {
        return super.canMoveToLocation(x, y, b, ignoreTurns) //basics
                && Math.abs((this.x - x)) == Math.abs(this.y - y) //any diagonal path
                && b.isValidPath(this.computePath(this.x, this.y, x, y));  //no hops
    }

    @Override
    public int[][] computePath(int startX, int startY, int finalX, int finalY) {
        ArrayList<int[]> path = new ArrayList();
        if (finalX < startX) 
             if (finalY < startY) while (startX != finalX) path.add(new int[]{startX--, startY--});
                             else while (startX != finalX) path.add(new int[]{startX--, startY++});
        else if (finalY < startY) while (startX != finalX) path.add(new int[]{startX++, startY--});
                             else while (startX != finalX) path.add(new int[]{startX++, startY++});
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