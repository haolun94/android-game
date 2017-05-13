package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class PlayerChooseGameActivity extends AppCompatActivity {

    protected SingletonData data;
    protected int gameIndex;
    private String gameName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_choose_game);
        data = SingletonData.getInstance(this);
        implementSpinner();
    }

    public void onStartGame(View view) {
        gameIndex = data.getGameIndex(gameName);
        if (gameIndex != -1) {
            // todo Retrieve the basic game SHOULD BE DELETED before turned in
            //data.retrieveBasicGame();
            // todo Retrieve the basic game SHOULD BE DELETED before turned in
            Intent intent = new Intent(this, PlayGameActivity.class);
            intent.putExtra("gameIndex", gameIndex);
            startActivity(intent);
        } else {
            Toast.makeText(view.getContext(), "You have not selected any game", Toast.LENGTH_SHORT).show();
        }
    }

    public void implementSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.player_game_spinner);
        List<String> gameList = data.getGameList();
        String[] spinnerContent = new String[gameList.size()];
        spinnerContent = gameList.toArray(spinnerContent);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gameName = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "You selected "+ gameName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
