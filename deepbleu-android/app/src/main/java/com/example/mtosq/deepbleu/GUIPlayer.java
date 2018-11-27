package com.example.mtosq.deepbleu;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents a player interacting with the GUI.
 *
 * @author Matthew Tosques
 */
public class GUIPlayer extends Player {

    //Another thread will eventually put a move in this queue, until then the game loop blocks.
    static final LinkedBlockingQueue<ChessMove> PUT_MOVE_HERE = new LinkedBlockingQueue(1);

    public GUIPlayer(String name, boolean isWhite) {
        super(name, isWhite);
    }

    @Override
    public ChessMove getMove(Board b) {
        try {
            return PUT_MOVE_HERE.take();
        } catch (InterruptedException ex) {
            return null;
        }
    }
}
