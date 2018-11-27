package com.example.mtosq.deepbleu;

import java.util.Scanner;

/**
 * Allows for text-based user input.
 *
 * @author Matthew Tosques
 */
public class ConsolePlayer extends Player {

    public ConsolePlayer(String name, boolean isWhite) {
        super(name, isWhite);
    }

    @Override
    public ChessMove getMove(Board b) {
        Scanner s = new Scanner(System.in);
        System.out.print("Move from: ");
        String fromLine = s.nextLine();
        char fromX = fromLine.charAt(0);
        int fromY = Integer.parseInt(fromLine.substring(1));
        System.out.print("Move " + fromLine + " to: ");
        String toLine = s.nextLine();
        char toX = toLine.charAt(0);
        int toY = Integer.parseInt(toLine.substring(1));
        return new ChessMove(fromX, fromY, toX, toY);
    }
}
