package deepbleu;
import java.sql.*; //todo...

public class LoginHandler {

	public static void main(String[] args) {
		Player playerOne = new ConsolePlayer("You", true);
	    Player playerTwo = new ComputerPlayer("deepbleu", false);
	    GameOfChess game = new GameOfChess(playerOne, playerTwo);
	    game.GAME_LOOP();
	}

}
