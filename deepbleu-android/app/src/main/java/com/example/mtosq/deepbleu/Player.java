package com.example.mtosq.deepbleu;

import java.io.Serializable;

/**
 * Abstract representation of a player.
 * Has a name, and either goes first or doesn't.
 *
 * @author Matthew Tosques
 */
public abstract class Player implements Serializable {
    public String name;
    public boolean isWhite;

    public Player(String name, boolean isWhite) {
        this.name = name;
        this.isWhite = isWhite;
    }

    public abstract ChessMove getMove(Board b);

    /**
     * @return Player's name
     */
    @Override
    public String toString() {
        return this.name;
    }
}
