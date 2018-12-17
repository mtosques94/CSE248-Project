package com.example.mtosq.deepbleu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.util.HashSet;

public class ChessBoardActivity extends AppCompatActivity {

    private Player playerOne = null;
    private Player playerTwo = null;

    static Board board = null;
    private ImageView[][] ImageBoard;
    //this gives the updateGraphics method something to look at without race conditions
    private Piece[][] myBoard = new Piece[8][8];

    Thread gameLoop = new Thread(() -> ChessBoardActivity.this.getWinner());
    boolean gameOver = false;

    private Runnable updateGraphics = new Runnable() {

        @Override
        public void run() {

            System.out.println("START GFX UPDATE");

            Piece selected = board.selected;

            for(int x=0;x<8;x++) {
                for (int y = 0; y < 8; y++) {
                    ImageView iv = ImageBoard[x][y];
                    if(myBoard[x][y] == null) {
                        iv.setImageResource(R.drawable.transparent);
                        iv.clearColorFilter();
                        iv.setScaleX(1f);
                        iv.setScaleY(1f);
                    }
                    else {
                        iv.setImageResource(myBoard[x][y].getDefaultImage());
                        if(selected != null && selected.x == x && selected.y == y) {
                            iv.setColorFilter(ContextCompat.getColor(iv.getContext(), R.color.colorPrimary), PorterDuff.Mode.DARKEN);
                            iv.setScaleX(1.15f);
                            iv.setScaleY(1.15f);
                        } else {
                            iv.clearColorFilter();
                            iv.setScaleX(1f);
                            iv.setScaleY(1f);
                        }
                    }
                }
            }
            if(selected != null) {
                for(int[] legalMoves : selected.getLegalMoves(board)) {
                    int x = legalMoves[0];
                    int y = legalMoves[1];
                    if(!board.causesCheck(new ChessMove(selected.x, selected.y, x, y)))
                        ImageBoard[x][y].setColorFilter(Color.GREEN, PorterDuff.Mode.OVERLAY);
                }
            }

            System.out.println("END GFX UPDATE");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_board);
        playerOne = (GUIPlayer) getIntent().getSerializableExtra("p1");
        //playerOne = new ComputerPlayer("localAI", true);

        playerTwo = new NetworkPlayer("Server", !playerOne.isWhite);

        board = new Board(playerOne, playerTwo);
        ImageBoard = new ImageView[8][8];
        for(int x=0;x<8;x++) {
            for(int y=0;y<8;y++) {

                String imgID;
                if(playerOne.isWhite)
                    imgID = "bgr" + (7-x) + "c" + y;
                else imgID = "bgr" + x + "c" + (7 - y);

                int resID = getResources().getIdentifier(imgID,
                        "id", getPackageName());

                ImageView tmp = findViewById(resID);

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
        this.updateGraphics();
        gameLoop.start();
    }

    private void updateGraphics() {

        for(int x=0;x<8;x++) {
            for (int y = 0; y < 8; y++) {
                myBoard[x][y] = board.tiles[x][y];
            }
        }

        runOnUiThread(updateGraphics);
    }

     /**
     * Allows legal moves until checkmate or draw. Returns the winning player.
     */
     Player getWinner() {
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
            if(gameOver) {
                return new ConsolePlayer("Game over.", false);
            }
        }
        return board.getWinner();
    }

    /**
     * Makes sure moves make sense before we send them to the board.
     */
    private void playValidMove() {
        boolean valid = false;
        while (!valid) {
            System.out.println(board);
            System.out.println(board.currentPlayer + "'s turn.  Total number of legal moves: "
                    + board.getAllLegalMoves().size());
            ChessMove potentialMove = board.currentPlayer.getMove(board);
            if(potentialMove == null) {
                gameOver = true;
                return;
            }
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

    public void logoutBtnPressed(View view) {
        Thread t = new Thread(() -> {
            NetworkPlayer p2n = (NetworkPlayer) this.playerTwo;
            p2n.writeLine("QUIT");
            Intent intent = new Intent(ChessBoardActivity.this, LoginActivity.class);
            startActivity(intent);

            try {
                p2n.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.gameLoop.interrupt();});
        t.start();
    }

}
