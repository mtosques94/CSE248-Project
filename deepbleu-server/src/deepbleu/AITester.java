package deepbleu;

import java.util.Date;

public class AITester {
	static Player p1 = new ComputerPlayer("white", true);
	static Player p2 = new ComputerPlayer("black", false);

	public static void test(int n) {

		long firstStartTime = System.currentTimeMillis();
		long totalTime = 0;
		
		int count = n;
		while (count-- > 0) {
			Board b = new Board(p1, p2);
			long startTime = System.currentTimeMillis();
			ChessMove w1 = b.currentPlayer.getMove(b);
			b.move(w1);
			ChessMove b1 = b.currentPlayer.getMove(b);
			b.move(b1);
			ChessMove w2 = b.currentPlayer.getMove(b);
			b.move(w2);
			ChessMove b2 = b.currentPlayer.getMove(b);
			b.move(b2);
			long finishTime = new Date().getTime() - startTime;
			totalTime += finishTime;
		}
		totalTime = totalTime / n;
		System.out.println("\n\nAverage time: " + totalTime / 1000d + " seconds with " + n + " tests.");
	}

}
