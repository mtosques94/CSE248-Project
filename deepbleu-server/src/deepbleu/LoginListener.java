package deepbleu;

import java.io.IOException;
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

	private static void listenUp() {
		try {
			listener = new ServerSocket(portNumber);
			Gson gson = new Gson();
			while (true) {
				System.out.println("LoginListener listening on port " + portNumber + "...");
				Socket clientSocket = listener.accept();
				lh.pendingLogins.add(clientSocket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
