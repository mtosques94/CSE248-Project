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
	private static GamePool gamePool = new GamePool();
	private static Thread gamePoolRunner = new Thread(gamePool);

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
				System.out.print(r.getString("username") + " ");
				System.out.println(r.getString("password"));
			}
			System.out.println("-----end of DB-----");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("Initializing GamePool...");
		gamePoolRunner.start();
	}

	@Override
	public void run() {
		while (true) {
			
			NetworkPlayer newGuy = null;
			GameOfChess newGame = null;
			
			try {
				System.out.println("LoginHandler ready for client connection...");
				Socket newConnection = pendingConnections.take();
				newGuy = new NetworkPlayer("Unauthorized User", false, newConnection);
				
				
				AuthData latestAuth = gson.fromJson(newGuy.readLine(), AuthData.class);
				System.out.println("LoginHandler parsed AuthPair: " + latestAuth);
				
				String sql = "SELECT * FROM Players WHERE username='" + latestAuth.getUsername()
					+ "' AND password= '" + latestAuth.getPassword() + "'\r\n";
				PreparedStatement p = null;
				ResultSet r = null;
				try {
					p = DB.prepareStatement(sql);
					p.clearParameters();
					r = p.executeQuery();

					if (!r.isBeforeFirst()) {
						System.out.println(latestAuth.toString() + " NOT FOUND");
						newGuy.writeLine("BAD");
						
					}
					else {

						newGuy.writeLine("GOOD");
						System.out.print(r.getString("username") + " ");
						System.out.println(r.getString("password"));
						
						newGuy = new NetworkPlayer(latestAuth.getUsername(), !latestAuth.isWhite(), newConnection);
						newGame = new GameOfChess(newGuy, new ComputerPlayer("deepbleu", latestAuth.isWhite()));
						try {
							System.out.println("Network game created!  Submitting to GamePool...");
							gamePool.addGame(newGame);
						}
						catch (Exception e ) {
							e.printStackTrace();
						}
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}

	}
}
