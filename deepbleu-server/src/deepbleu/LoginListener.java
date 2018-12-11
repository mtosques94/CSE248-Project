package deepbleu;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LoginListener {

	private static int portNumber = 1994;
	private static ServerSocket listener;
	private static LoginHandler lh = new LoginHandler();
	private static Thread handler = new Thread(lh);

	public static void run(String[] args) {
		handler.start();
		listenUp();
	}

	private static void listenUp() {
		try {
			listener = new ServerSocket(portNumber);
			while (true) {
				System.out.println("LoginListener listening on port " + portNumber + "...");
				Socket clientSocket = listener.accept();
				LoginHandler.pendingConnections.add(clientSocket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
