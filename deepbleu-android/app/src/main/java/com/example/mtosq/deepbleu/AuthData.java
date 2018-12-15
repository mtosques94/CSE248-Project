package com.example.mtosq.deepbleu;

public class AuthData {

    private String username;
    private String password;
    private boolean playAsWhite;
    private boolean createNewAcct;

    public AuthData(String username, String password, boolean playAsWhite, boolean createNewAcct) {
        this.username = username;
        this.password = password;
        this.playAsWhite = playAsWhite;
        this.createNewAcct = createNewAcct;
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

    public boolean isNew() {
        return createNewAcct;
    }

    @Override
    public String toString() {
        return "AuthData [username=" + username + ", password=" + password + ", playAsWhite=" + playAsWhite
                + ", createNewAcct=" + createNewAcct + "]";
    }

}