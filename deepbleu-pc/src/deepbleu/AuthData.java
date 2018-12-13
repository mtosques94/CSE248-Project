package deepbleu;

public class AuthData {

	private String username;
	private String password;
	private boolean playAsWhite;

	public AuthData(String username, String password, boolean playAsWhite) {
		this.username = username;
		this.password = password;
		this.playAsWhite = playAsWhite;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public boolean isWhite() {
		return playAsWhite;
	}

	@Override
	public String toString() {
		return "AuthPair [username=" + username + ", password=" + password + "]";
	}

}
