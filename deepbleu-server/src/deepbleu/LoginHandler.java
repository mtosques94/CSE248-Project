package deepbleu;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

				String userPassSQL = "SELECT * FROM Players WHERE username='" + latestAuth.getUsername()
						+ "' AND password= '" + latestAuth.getPassword() + "'\r\n";
				String userSQL = "SELECT * FROM Players WHERE username='" + latestAuth.getUsername() + "'\r\n";

				PreparedStatement psUserPass = null;
				ResultSet userPassResult = null;
				PreparedStatement psUser = null;
				ResultSet userResult = null;

				try {
					psUserPass = DB.prepareStatement(userPassSQL);
					psUserPass.clearParameters();
					userPassResult = psUserPass.executeQuery();

					psUser = DB.prepareStatement(userSQL);
					psUser.clearParameters();
					userResult = psUserPass.executeQuery();

					// account not found
					if (!userPassResult.isBeforeFirst()) {
						System.out.println(latestAuth.toString() + " NOT FOUND");
						if (latestAuth.isNew()) {
							if (!userResult.isBeforeFirst()) {
								newGuy.writeLine("BAD");
							} else {
								PreparedStatement prep = DB.prepareStatement("insert into Players values (?, ?);");
								prep.clearParameters();
								prep.setString(1, latestAuth.getUsername());
								prep.setString(2, latestAuth.getPassword());
								prep.executeUpdate();
								newGuy.writeLine("GOOD");
								newGuy = new NetworkPlayer(latestAuth.getUsername(), !latestAuth.isWhite(),
										newConnection);
								newGame = new GameOfChess(newGuy, new ComputerPlayer("deepbleu", latestAuth.isWhite()));

								try {
									System.out.println("Network game created!  Submitting to GamePool...");
									gamePool.addGame(newGame);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

						} else {
							newGuy.writeLine("BAD");
							newGuy.disconnect();
						}
					}

					// account already exists
					else {
						if (latestAuth.isNew()) {
							newGuy.writeLine("BAD");
							newGuy.disconnect();
						} else {
							newGuy.writeLine("GOOD");

							newGuy = new NetworkPlayer(latestAuth.getUsername(), !latestAuth.isWhite(), newConnection);
							newGame = new GameOfChess(newGuy, new ComputerPlayer("deepbleu", latestAuth.isWhite()));
							try {
								System.out.println("Network game created!  Submitting to GamePool...");
								gamePool.addGame(newGame);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
