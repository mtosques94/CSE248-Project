package deepbleu;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.google.gson.Gson;

/**
 * @author Matthew Tosques
 * 
 *  TO DO:
 *      - Regarding Chess: 
 *          * En Passant, Castling, Pawn Promotion, 50 Move Rule, Threefold Repetition. *          
 */

public class GameOfChess implements Callable<EndGameState> {
	
	Board BOARD;
	
	Gson gson = new Gson();
	
	public GameOfChess(Player playerOne, Player playerTwo) {
		BOARD = new Board(playerOne, playerTwo);
	}

	@Override
	public EndGameState call() throws Exception {
		Player winner = this.getWinner();
		boolean wasDraw = winner instanceof ConsolePlayer && winner.name.equals("DRAW");
		EndGameState theEnd = new EndGameState(BOARD.player1, BOARD.player2, winner, wasDraw);
		return theEnd;
	}

    /**
     * Allows legal moves until checkmate or draw. Returns the winning player.
     */
    Player getWinner() {
        while (!(BOARD.hasDraw() || BOARD.kingCaptured())) {
            Collection<ChessMove> allLegalMoves = BOARD.getAllLegalMoves();
            if (allLegalMoves.isEmpty()) {
                return this.BOARD.getWinner();
            }
            if (BOARD.hasCheck()) { //if current player is in check
                boolean canExitCheck = false;
                for (ChessMove legalMove : allLegalMoves) { //see if any move gets current player to safety
                    BOARD.move(legalMove); //simulate move
                    BOARD.switchTurns();
                    if (!canExitCheck) {
                        canExitCheck = !BOARD.hasCheck();
                    }
                    BOARD.switchTurns();
                    BOARD.undoLastMove();
                }
                if (!canExitCheck) { //if all moves leave player in check the game is over
                    return BOARD.getWinner();
                }
            }
            playValidMove();
            System.out.println("Valid move played.");           
        }
        return BOARD.getWinner();
    }

    /**
     * Makes sure moves make sense before we send them to the board.
     */
    ChessMove playValidMove() {
        boolean valid = false;
        while (!valid) {
            System.out.println(BOARD);
            System.out.println(this.BOARD.currentPlayer + "'s turn.  Total number of legal moves: "
                    + BOARD.getAllLegalMoves().size());
            ChessMove potentialMove = BOARD.currentPlayer.getMove(BOARD);
            if (!(BOARD.currentPlayer instanceof ConsolePlayer)
                    || BOARD.isLegalMove(potentialMove)) {
                System.out.print("Final decision: " + BOARD.currentPlayer + " moved " + potentialMove + ".  \n");
                for (Piece[] row : BOARD.tiles) {
                    for (Piece p : row) {
                        if (p != null && p.x == potentialMove.toRow && p.y == potentialMove.toCol) {
                            System.out.println("MOVE CAPTURED PIECE: " + p.getClass().getSimpleName());
                        }
                    }
                }
                System.out.println("\n");
                BOARD.move(potentialMove);
                valid = true;
                return potentialMove;
            } else {
                System.out.println(BOARD);
                System.out.println("Invalid move.");
            }
        }
        return null;
    }
}
