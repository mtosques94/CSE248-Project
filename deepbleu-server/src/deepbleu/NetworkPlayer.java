package deepbleu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import com.google.gson.Gson;

public class NetworkPlayer extends Player {

	private Socket clientConnection = new Socket();
	Gson gson = new Gson();

	public NetworkPlayer(String name, boolean isWhite) {
		super(name, isWhite);
	}

	public NetworkPlayer(String name, boolean isWhite, Socket clientConnection) {
		super(name, isWhite);
		this.clientConnection = clientConnection;
	}

	public void connect() {
		try {
			InetAddress ia = InetAddress.getByName("localhost");
			SocketAddress sa = new InetSocketAddress(ia, 1994);
			this.clientConnection.connect(sa);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void connect(String addr, int port) {
		try {
			InetAddress ia = InetAddress.getByName(addr);
			SocketAddress sa = new InetSocketAddress(ia, port);
			this.clientConnection.connect(sa);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Socket getSocket() {
		return this.clientConnection;
	}

	@Override
	public ChessMove getMove(Board b) {
		try {
			InputStreamReader isr = new InputStreamReader(clientConnection.getInputStream());
			BufferedReader reader = new BufferedReader(isr);
			String line = null;
			while (line == null)
				line = reader.readLine();
			System.out.println("Network player read this line: " + line);
			ChessMove networkMove = gson.fromJson(line, ChessMove.class);
			return networkMove;
		} catch (IOException e) {
			return null;
		}
	}

}
