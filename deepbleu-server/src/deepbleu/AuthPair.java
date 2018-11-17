package deepbleu;

public class AuthPair {
	
	private String username;
	private String password;
	
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
