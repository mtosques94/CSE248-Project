package deepbleu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;

public class LoginListener {

	private static int portNumber = 1994;
	private static LoginHandler lh = new LoginHandler();
	private static ServerSocket listener;
	private static Thread handler = new Thread(lh);

	public static void main(String[] args) {
		handler.start();
		listenUp();
	}

	@SuppressWarnings("resource")
	private static void listenUp() {
		try {
			listener = new ServerSocket(portNumber);
			Gson gson = new Gson();
			while (true) {
				System.out.println("LoginListener listening on port " + portNumber + "...");
				Socket clientSocket = listener.accept();
				InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
				BufferedReader reader = new BufferedReader(isr);
				String line = reader.readLine();
				while (!line.isEmpty()) {
					System.out.println(line);
					AuthPair loginAttempt = gson.fromJson(line, AuthPair.class);
					lh.pendingLogins.add(loginAttempt);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
