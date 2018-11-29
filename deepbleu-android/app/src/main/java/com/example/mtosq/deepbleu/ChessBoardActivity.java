package com.example.mtosq.deepbleu;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.mtosq.deepbleu.pieces.Bishop;

import java.lang.reflect.Field;

public class ChessBoardActivity extends AppCompatActivity {

    Player playerOne = new GUIPlayer("You",true);
    Player playerTwo = new GUIPlayer("Opponent",false);

    Board board = new Board(playerOne, playerTwo);
    ImageView[][] ImageBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_board);

        ImageBoard = new ImageView[8][8];
        for(int x=0;x<8;x++) {
            for(int y=0;y<8;y++) {

                String tmpID = "bgr" + (7-x) + "c" + y;

                int resID = getResources().getIdentifier(tmpID,
                        "id", getPackageName());

                ImageView tmp = (ImageView) findViewById(resID);

                ImageBoard[x][y] = tmp;

                Piece p = board.tiles[x][y];
                if(p != null) {
                    tmp.setImageResource(p.getDefaultImage());
                }

            }
        }
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
