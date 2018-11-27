package deepbleu;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GamePool implements Runnable {

	private static int maxGames;

	private static ExecutorService ES;
	private static ExecutorCompletionService<EndGameState> ECS;

	public GamePool() {
		GamePool.maxGames = 10;
		GamePool.ES = Executors.newFixedThreadPool(maxGames);
		ECS = new ExecutorCompletionService<EndGameState>(ES);
	}
	
	public GamePool(int maxGames) {
		GamePool.maxGames = maxGames;
	}

	public void addGame(GameOfChess newGame) {
		ECS.submit(newGame);
	}

	@Override
	public void run() {
		while (true) {
			try {
				EndGameState mostRecentEndGame = ECS.take().get();
				System.out.println("GamePool says: " + mostRecentEndGame.getWinner() + " has won a match against "
						+ mostRecentEndGame.getLoser() + ".");
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

}
