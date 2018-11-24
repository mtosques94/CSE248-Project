package deepbleu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;

public class LoginHandler implements Runnable {

	static LinkedBlockingQueue<Socket> pendingConnections = new LinkedBlockingQueue<Socket>();
	private static Connection DB;
	private static Gson gson = new Gson();

	public LoginHandler() {
		// Load SQLite driver
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Connect to the database
		try {
			DB = DriverManager.getConnection("jdbc:sqlite:PlayerDB.sqlite3");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Print all Players
		String sql = "SELECT * from Players";
		PreparedStatement p = null;
		ResultSet r = null;

		try {
			p = DB.prepareStatement(sql);
			p.clearParameters();
			r = p.executeQuery();

			System.out.println("Working with the following AuthPairs:");
			while (r.next()) {
				System.out.print(r.getString("name") + " ");
				System.out.println(r.getString("password"));
			}
			System.out.println("-----end of DB-----");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		while (true) {
			
			NetworkPlayer newGuy = null;
			GameOfChess newGame = null;
			
			try {
				System.out.println("LoginHandler ready for client connections...");
				Socket clientConnection = pendingConnections.take();
				InputStreamReader isr = new InputStreamReader(clientConnection.getInputStream());
				BufferedReader reader = new BufferedReader(isr);
				String line = reader.readLine();
				System.out.println(line);
				AuthPair latestAuth = gson.fromJson(line, AuthPair.class);
				System.out.println("LoginHandler parsed AuthPair: " + latestAuth);

				newGuy = new NetworkPlayer(latestAuth.getUsername(), true, clientConnection);
				newGame = new GameOfChess(newGuy, new ComputerPlayer("deepbleu", false));

			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
			
			try {
				System.out.println("Network game created!  Starting now...");
				System.out.println("The winner is: " + newGame.call());
			}
			catch (Exception e ) {
				e.printStackTrace();
			}
		}

	}
}
