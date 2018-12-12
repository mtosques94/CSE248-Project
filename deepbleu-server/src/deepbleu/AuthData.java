package deepbleu;

public class AuthData {

	private String username;
	private String password;

	public AuthData(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "AuthPair [username=" + username + ", password=" + password + "]";
	}

}