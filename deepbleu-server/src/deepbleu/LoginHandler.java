package deepbleu;

import java.sql.*;

public class LoginHandler {
	
	public static void main(String[] args) {
		
		//Load SQLite driver
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//Connect to the database
		Connection c = null;
		try {
			c = DriverManager.getConnection("jdbc:sqlite:PlayerDB.sqlite3");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Print all Players
		String sql = "SELECT * from Players";
		PreparedStatement p = null;
		ResultSet r = null;
		
		try {
			p = c.prepareStatement(sql);
			p.clearParameters();
			r = p.executeQuery();
			
			while(r.next()) {
				System.out.print(r.getString("name") + " ");
				System.out.println(r.getString("password"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	//this will need to be in a new thread
	private static void startGame(Player playerOne, Player playerTwo) {
		GameOfChess game = new GameOfChess(playerOne, playerTwo);
		game.GAME_LOOP();
	}

}
