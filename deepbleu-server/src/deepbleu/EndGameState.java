package deepbleu;

public class EndGameState {
	
	private Player playerOne = null;
	private Player playerTwo = null;
	private Player winningPlayer = null;
	private boolean wasDraw = false;

	public EndGameState(Player playerOne, Player playerTwo, Player winningPlayer, boolean wasDraw) {
		this.playerOne = playerOne;
		this.playerTwo = playerTwo;
		this.winningPlayer = winningPlayer;
		this.wasDraw = wasDraw;
	}
	
	public boolean wasDraw() {
		return this.wasDraw;
	}
	
	public Player getWinner() {
		return this.winningPlayer;
	}

}
