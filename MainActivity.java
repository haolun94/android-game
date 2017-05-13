package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onPlayGame(View view) {
        Intent intent = new Intent(this, PlayerChooseGameActivity.class);
        startActivity(intent);
    }

    public void onEditGame(View view) {
        Intent intent = new Intent(this,MainChooseActivity.class);
        startActivity(intent);
    }
    public void onReset(View view) {
        SingletonData data = SingletonData.getInstance(this);
        data.reset();
    }


}
