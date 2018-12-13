package com.example.mtosq.deepbleu;

import android.content.Intent;
import android.net.Network;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void loginBtnPressed (View view) {

        TextView userNameField = findViewById(R.id.userNameField);
        TextView passwordField = findViewById(R.id.passwordField);

        String username = userNameField.getText().toString();
        String password = passwordField.getText().toString();

        Player p1 = new GUIPlayer(username, false);
        Player p2 = new NetworkPlayer("Server", true);

        Thread loginThread = new Thread(() -> {
            try  {
                NetworkPlayer p2n = (NetworkPlayer) p2;
                p2n.connect("10.0.2.2", 1994, username, password, true);
                String response = p2n.readLine().trim();

                if(response.equals("GOOD")) {
                    Intent intent = new Intent(LoginActivity.this, ChessBoardActivity.class);
                    intent.putExtra("p1", p1);
                    startActivity(intent);
                } else if(response.equals("BAD")) {
                    p2n.disconnect();
                } else {
                    p2n.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loginThread.start();

    }
}
