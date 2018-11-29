package com.example.mtosq.deepbleu;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.mtosq.deepbleu.pieces.Bishop;

import java.lang.reflect.Field;
import java.util.HashSet;

public class ChessBoardActivity extends AppCompatActivity {

    static Player playerOne = new GUIPlayer("You",true);
    static Player playerTwo = new GUIPlayer("Opponent",false);

    static Board board = new Board(playerOne, playerTwo);
    static ImageView[][] ImageBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_board);

        ImageBoard = new ImageView[8][8];
        for(int x=0;x<8;x++) {
            for(int y=0;y<8;y++) {

                String tmpID = "bgr" + (7-x) + "c" + y;

                int resID = getResources().getIdentifier(tmpID,
                        "id", getPackageName());

                ImageView tmp = (ImageView) findViewById(resID);

                ImageBoard[x][y] = tmp;

                Piece p = board.tiles[x][y];
                if(p != null) {
                    tmp.setImageResource(p.getDefaultImage());
                }

                final int finalX = x;
                final int finalY = y;
                tmp.setOnClickListener(event -> {
                    if (board.currentPlayer instanceof GUIPlayer && board.enabled) {
                        if (board.tiles[finalX][finalY] != null
                                && board.tiles[finalX][finalY].belongsToCurrentPlayer(board)) {//if you click on your own piece
                            board.selected = board.tiles[finalX][finalY]; //you pick it up
                            this.updateGraphics();
                        } else  { // if you click on an empty tile or an enemy
                            if (board.selected != null) { //and you have selected something already
                                if (board.selected.canMoveToLocation(finalX, finalY, board) //if you can move it there
                                        && (!board.GUI_EXIT_CHECK
                                        || !board.causesCheck(new ChessMove(board.selected.x,board.selected.y,finalX,finalY)))) {
                                    System.out.println("Adding move.");
                                    GUIPlayer.PUT_MOVE_HERE.add( //do it
                                            new ChessMove(board.selected.x,board.selected.y,finalX,finalY));
                                    board.selected = null; //deselect piece after move
                                }
                            }
                        }
                    }
                });

            }
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ChessBoardActivity.getWinner();
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    public static void updateGraphics() {
        for(int x=0;x<8;x++) {
            for (int y = 0; y < 8; y++) {
                ImageView iv = ImageBoard[x][y];
                if(board.tiles[x][y] == null) {
                    iv.setImageResource(R.drawable.transparent);
                }
                else {
                    iv.setImageResource(board.tiles[x][y].getDefaultImage());
                }
            }
        }

    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Allows legal moves until checkmate or draw. Returns the winning player.
     */
    static Player getWinner() {
        while (!(board.hasDraw() || board.kingCaptured())) {
            HashSet<ChessMove> allLegalMoves = board.getAllLegalMoves();
            if (allLegalMoves.isEmpty()) {
                return board.getWinner();
            }
            if (board.hasCheck()) { //if current player is in check
                //CHECK_ICON.setOpacity(100); //display check icon in the toolbar
                boolean canExitCheck = false;
                for (ChessMove legalMove : allLegalMoves) { //see if any move gets current player to safety
                    board.move(legalMove); //simulate move
                    board.switchTurns();
                    if (!canExitCheck) {
                        canExitCheck = !board.hasCheck();
                    }
                    board.switchTurns();
                    board.undoLastMove();
                }
                if (!canExitCheck) { //if all moves leave player in check the game is over
                    return board.getWinner();
                }
            } else {
                //CHECK_ICON.setOpacity(0);
            }
            playValidMove();
            updateGraphics();
        }
        return board.getWinner();
    }

    /**
     * Makes sure moves make sense before we send them to the board.
     */
    static void playValidMove() {
        boolean valid = false;
        while (!valid) {
            System.out.println(board);
            System.out.println(board.currentPlayer + "'s turn.  Total number of legal moves: "
                    + board.getAllLegalMoves().size());
            ChessMove potentialMove = board.currentPlayer.getMove(board);
            if (!(board.currentPlayer instanceof ConsolePlayer)
                    || board.isLegalMove(potentialMove)) {
                System.out.print("Final decision: " + board.currentPlayer + " moved " + potentialMove + ".  \n");
                for (Piece[] row : board.tiles) {
                    for (Piece p : row) {
                        if (p != null && p.x == potentialMove.toRow && p.y == potentialMove.toCol) {
                            System.out.println("MOVE CAPTURED PIECE: " + p.getClass().getSimpleName());
                        }
                    }
                }
                System.out.println("\n");
                //MOVE_HISTORY.appendText(BOARD.currentPlayer + " moved " + potentialMove + "\n");
                board.move(potentialMove);
                valid = true;
            } else {
                System.out.println(board);
                System.out.println("Invalid move.");
            }
        }
    }


}
