package deepbleu;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Intended to be written to a file immediately.
 * @author Matthew Tosques
 */

public class SaveState implements Serializable {
    
    private final Player playerOne;
    private final Player playerTwo;
    private final Player currentPlayer;
    private final Piece[][] tiles;
    private final ArrayList<ChessMove> moveHistory;
    private String moveHistoryText;

    public SaveState(Board b) {
        this.playerOne = b.player1;
        this.playerTwo = b.player2;
        this.currentPlayer = b.currentPlayer;
        this.tiles = b.tiles;
        this.moveHistory = b.moveHistory;
    }
    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Piece[][] getTiles() {
        return tiles;
    }

    public ArrayList<ChessMove> getMoveHistory() {
        return moveHistory;
    }

    public String getMoveHistoryText() {
        return moveHistoryText;
    }

    public void setMoveHistoryText(String moveHistoryText) {
        this.moveHistoryText = moveHistoryText;
    }
    
}