package com.example.mtosq.deepbleu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void loginBtnPressed(View view) {

        TextView userNameField = findViewById(R.id.userNameField);
        TextView passwordField = findViewById(R.id.passwordField);
        CheckBox isBlackCheckBox = findViewById(R.id.isBlackCheckBox);
        CheckBox createAccountCheckBox = findViewById(R.id.createAccountCheckBox);

        String username = userNameField.getText().toString();
        String password = passwordField.getText().toString();
        boolean isBlack = isBlackCheckBox.isChecked();
        boolean isNew = createAccountCheckBox.isChecked();

        Player p1 = new GUIPlayer(username, !isBlack);
        Player p2 = new NetworkPlayer("Server", isBlack);

        Thread loginThread = new Thread(() -> {
            try {
                //connect to server
                NetworkPlayer p2n = (NetworkPlayer) p2;
                System.out.println("Network player connecting.  New acct = " + isNew);
                p2n.connect("10.0.2.2", 1994, username, password, isBlack, isNew);
                String response = p2n.readLine().trim();

                //server accepted user
                if (response.equals("GOOD")) {
                    Intent intent = new Intent(LoginActivity.this, ChessBoardActivity.class);
                    intent.putExtra("p1", p1);
                    startActivity(intent);
                    finish();
                }

                //server denied user
                else if (response.equals("BAD")) {
                    p2n.disconnect();
                    if(isNew) {
                        //account already exists
                        runOnUiThread(() -> Toast.makeText(getBaseContext(), "Account already exists.", Toast.LENGTH_SHORT).show());
                    } else {
                        //account not found
                        runOnUiThread(() -> Toast.makeText(getBaseContext(), "Account not found.", Toast.LENGTH_SHORT).show());
                    }
                }

                //server spoke gibberish
                else {
                    p2n.disconnect();
                    runOnUiThread(() -> Toast.makeText(getBaseContext(), "Error communicating with server.", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loginThread.start();

    }
}
