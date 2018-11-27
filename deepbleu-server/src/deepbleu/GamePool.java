package deepbleu;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GamePool implements Runnable {

	// Use as many threads as possible, up to the number of logical CPUs present
	private static ExecutorService ES = Executors
			.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 0, 1));
	// This has the completed work returned to a blocking queue in order of
	// completion.
	private static ExecutorCompletionService<EndGameState> ECS = new ExecutorCompletionService(ES);

	public GamePool() {

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
