package deepbleu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.google.gson.Gson;

public class NetworkPlayer extends Player {

	Socket clientConnection;
	Gson gson = new Gson();

	public NetworkPlayer(String name, boolean isWhite, Socket clientConnection) {
		super(name, isWhite);
		this.clientConnection = clientConnection;
	}

	@Override
	public ChessMove getMove(Board b) {
		try {
			InputStreamReader isr = new InputStreamReader(clientConnection.getInputStream());
			BufferedReader reader = new BufferedReader(isr);
			String line = reader.readLine();
			System.out.println(line);
			ChessMove networkMove = gson.fromJson(line, ChessMove.class);
			return networkMove;
		} catch (IOException e) {
			return null;
		}
	}

}
