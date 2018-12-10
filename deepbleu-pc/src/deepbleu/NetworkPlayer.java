package deepbleu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import com.google.gson.Gson;

public class NetworkPlayer extends Player {

	private static Socket clientConnection = new Socket();
	private static BufferedReader buffIn = null;
	private static BufferedWriter buffOut = null;

	Gson gson = new Gson();

	public NetworkPlayer(String name, boolean isWhite) {
		super(name, isWhite);
	}

	public NetworkPlayer(String name, boolean isWhite, Socket clientConnection) {
		super(name, isWhite);
		NetworkPlayer.clientConnection = clientConnection;
		try {
			buffIn = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
			buffOut = new BufferedWriter(new OutputStreamWriter(clientConnection.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect() {
		try {
			InetAddress ia = InetAddress.getByName("10.0.2.2");
			SocketAddress sa = new InetSocketAddress(ia, 1994);
			clientConnection.connect(sa);
			buffIn = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
			buffOut = new BufferedWriter(new OutputStreamWriter(clientConnection.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect(String addr, int port) {
		try {
			InetAddress ia = InetAddress.getByName(addr);
			SocketAddress sa = new InetSocketAddress(ia, port);
			clientConnection.connect(sa);
			buffIn = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
			buffOut = new BufferedWriter(new OutputStreamWriter(clientConnection.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect(String addr, int port, String username, String password) {
		try {
			InetAddress ia = InetAddress.getByName(addr);
			SocketAddress sa = new InetSocketAddress(ia, port);
			clientConnection.connect(sa);
			buffIn = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
			buffOut = new BufferedWriter(new OutputStreamWriter(clientConnection.getOutputStream()));
			AuthPair logMeIn = new AuthPair(username, password);
			this.writeJson(logMeIn);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void writeLine(String line) {
		try {
			buffOut.write(line.trim());
			buffOut.newLine();
			buffOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeJson(Object obj) {
		String line = gson.toJson(obj);
		this.writeLine(line);
	}

	public String readLine() {
		String line = null;

		while (line == null) {
			try {
				line = buffIn.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		System.out.println("Network player read this line: " + line);
		return line;
	}

	@Override
	public ChessMove getMove(Board b) {
		if (b.moveHistory.size() > 0) {
			ChessMove mostRecentMove = new ChessMove(b.moveHistory.get(b.moveHistory.size() - 1));
			mostRecentMove.enemyCaptured = null;
			this.writeJson(mostRecentMove);
		}
		System.out.println("NetworkPlayer trying to read line...");
		ChessMove networkMove = gson.fromJson(this.readLine(), ChessMove.class);
		return networkMove;
	}

}