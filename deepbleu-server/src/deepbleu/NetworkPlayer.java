package deepbleu;

import java.util.concurrent.LinkedBlockingQueue;

public class NetworkPlayer extends Player {
	
	final LinkedBlockingQueue<ChessMove> PUT_MOVE_HERE = new LinkedBlockingQueue(1);
	//plus some network stuff

	public NetworkPlayer(String name, boolean isWhite) {
		super(name, isWhite);
	}

	@Override
	public ChessMove getMove(Board b) {
		try {
			return PUT_MOVE_HERE.take();
		} catch (InterruptedException e) {
			return null;
		}
	}

}
