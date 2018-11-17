package deepbleu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*; //to do...

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class LoginHandler {

	static final int portNumber = 1994;

	public static void main(String[] args) {
		
		try {
			/*
			ServerSocket ss = new ServerSocket(portNumber);
			ss.setSoTimeout(0); // Socket never closes
			System.out.println("listening...");
			Socket temp = ss.accept(); // Listen for incoming connection
			temp.setSoTimeout(120000); // Timeout 2 minutes (set to what you want)
			BufferedReader br = new BufferedReader(new InputStreamReader(temp.getInputStream()));
			String content = br.readLine();
			System.out.println(content);
			ss.close();
			*/
			JsonObject json = new JsonObject();
			json.addProperty("username","john");
			json.addProperty("password", "doe");
			String content = json.toString();
			
			AuthPair ap = new Gson().fromJson(content, AuthPair.class);
			System.out.println(ap);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		Player playerOne = new ConsolePlayer("You", true);
		Player playerTwo = new ComputerPlayer("deepbleu", false);
		GameOfChess game = new GameOfChess(playerOne, playerTwo);
		game.GAME_LOOP();
		*/
		
	}

}
