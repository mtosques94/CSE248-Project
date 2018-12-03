package com.example.mtosq.deepbleu;

public class AuthPair {

    private String username;
    private String password;

    public AuthPair(String username, String password) {
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
