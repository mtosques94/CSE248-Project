package com.example.mtosq.deepbleu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void loginBtnPressed (View view) {

        TextView userNameField = findViewById(R.id.userNameField);
        TextView passwordField = findViewById(R.id.passwordField);
        CheckBox isBlackCheckBox = findViewById(R.id.isBlackCheckBox);

        String username = userNameField.getText().toString();
        String password = passwordField.getText().toString();
        boolean isBlack = isBlackCheckBox.isChecked();

        Player p1 = new GUIPlayer(username, !isBlack);
        Player p2 = new NetworkPlayer("Server", isBlack);

        Thread loginThread = new Thread(() -> {
            try  {
                NetworkPlayer p2n = (NetworkPlayer) p2;
                p2n.connect("10.0.2.2", 1994, username, password, isBlack);
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
