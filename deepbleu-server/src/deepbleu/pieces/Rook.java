package deepbleu.pieces;

import deepbleu.Board;
import deepbleu.Piece;
import deepbleu.Player;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Matthew Tosques
 */
public class Rook extends Piece {

    //static final Image DEFAULT_WHITE = new Image("/img/small/WhiteRook.png");
    //static final Image DEFAULT_BLACK = new Image("/img/small/BlackRook.png");
    static final int[][] ALL_DELTAS = new int[][] {{-1,0},{0,1},{1,0},{0,-1}};

    public Rook(int x, int y, Player p) {
        super(x, y, p);
    }
    
    //copy constructor
    public Rook(Rook p) {
        super(p);
    }
    
    @Override
    public char getChar() {
        if (this.player.isWhite)
            return 'R';
        return 'r';
    }
    
    /*
    @Override
        public Image getDefaultImage() {
        if(this.player.isWhite)
            return DEFAULT_WHITE;
        return DEFAULT_BLACK;
    }
    */

    @Override
    public boolean canMoveToLocation(int x, int y, Board b, boolean ignoreTurns) {
        return super.canMoveToLocation(x, y, b, ignoreTurns) //basics
                && (this.x - x == 0 || this.y - y == 0) //any straight path
                && b.isValidPath(this.computePath(this.x, this.y, x, y)); //no hops
    }

    @Override
    public int[][] computePath(int startX, int startY, int finalX, int finalY) {
        ArrayList<int[]> path = new ArrayList();
             if (finalX < startX) while (startX != finalX) path.add(new int[]{startX--, startY});
        else if (finalX > startX) while (startX != finalX) path.add(new int[]{startX++, startY});
        else if (finalY < startY) while (startY != finalY) path.add(new int[]{startX, startY--});
        else if (finalY > startY) while (startY != finalY) path.add(new int[]{startX, startY++});
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
