package deepbleu;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;

public class LoginHandler implements Runnable {

	LinkedBlockingQueue<AuthPair> pendingLogins = new LinkedBlockingQueue<AuthPair>();
	private Connection DB;

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
			try {
				System.out.println("LoginHandler ready for AuthPairs...");
				System.out.println(pendingLogins.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
