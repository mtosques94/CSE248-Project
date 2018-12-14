package com.example.mtosq.deepbleu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.HashSet;

public class ChessBoardActivity extends AppCompatActivity {

    private Player playerOne = null;
    private Player playerTwo = null;

    private Board board = null;
    private ImageView[][] ImageBoard;
    //this gives the updateGraphics method something to look at without race conditions
    private Piece[][] myBoard = new Piece[8][8];

    private Runnable updateGraphics = new Runnable() {
        @Override
        public void run() {

            System.out.println("START GFX UPDATE");

            for(int x=0;x<8;x++) {
                for (int y = 0; y < 8; y++) {
                    ImageView iv = ImageBoard[x][y];
                    if(myBoard[x][y] == null) {
                        iv.setImageResource(R.drawable.transparent);
                    }
                    else {
                        iv.setImageResource(myBoard[x][y].getDefaultImage());
                    }
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

        board = new Board(playerOne,playerTwo);
        ImageBoard = new ImageView[8][8];
        for(int x=0;x<8;x++) {
            for(int y=0;y<8;y++) {

                String imgID;
                if(playerOne.isWhite)
                    imgID = "bgr" + (7-x) + "c" + y;
                else imgID = "bgr" + x + "c" + y;

                int resID = getResources().getIdentifier(imgID,
                        "id", getPackageName());

                ImageView tmp = findViewById(resID);

                ImageBoard[x][y] = tmp;

                Piece p = board.tiles[x][y];
                if(p != null) {
                    tmp.setImageResource(p.getDefaultImage());
                }

                if(!playerOne.isWhite) {
                    if(x % 2 == 0) {
                        if(y % 2 == 0) {
                            tmp.setBackgroundResource(R.drawable.purewhite);
                        } else {
                            tmp.setBackgroundResource(R.drawable.puregrey);
                        }
                    } else {
                        if(y % 2 == 0) {
                            tmp.setBackgroundResource(R.drawable.puregrey);
                        } else {
                            tmp.setBackgroundResource(R.drawable.purewhite);
                        }
                    }
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

        Runnable r = new Runnable() {
            @Override
            public void run() {
                ChessBoardActivity.this.getWinner();
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    public void updateGraphics() {

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
        }
        return board.getWinner();
    }

    /**
     * Makes sure moves make sense before we send them to the board.
     */
    void playValidMove() {
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
