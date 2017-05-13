package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class MainChooseActivity extends AppCompatActivity {

    protected static SingletonData data;
    protected String gameName;
    protected int gameIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_choose);
        data = SingletonData.getInstance(this);
        implementSpinner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        implementSpinner();
    }

    public void onCreateNewGame(View view){
        EditText editText = (EditText) findViewById(R.id.edit_game_name);
        String gameNameStr = editText.getText().toString();
        if (gameNameStr.isEmpty()) {
            Toast.makeText(this, "Game name cannot be empty", Toast.LENGTH_LONG).show();
        } else {

            if (!data.addNewGame(gameNameStr)) {
                Toast.makeText(this, gameNameStr + " already exists", Toast.LENGTH_LONG).show();
                return;
            }
            gameName = gameNameStr;
            gameIndex = data.getGameIndex(gameName);
            Intent intent = new Intent(this, MainEditActivity.class);
            intent.putExtra("lastPageIndex", 0);
            intent.putExtra("gameIndex", gameIndex);
            startActivity(intent);
        }

    }

    public void onOpenGame(View view){
        int lastPageIndex = data.getLastPageIndex(gameName);
        gameIndex = data.getGameIndex(gameName);
        Intent intent = new Intent(this, MainEditActivity.class);
        intent.putExtra("gameIndex", gameIndex);
        intent.putExtra("lastPageIndex", lastPageIndex);
        Toast.makeText(this, "You selected "+ gameName, Toast.LENGTH_LONG).show();
        startActivity(intent);
    }

    public void implementSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.game_spinner);
        List<String> gameList = data.getGameList();
        String[] spinnerContent = new String[gameList.size()];
        spinnerContent = gameList.toArray(spinnerContent);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gameName = parent.getItemAtPosition(position).toString();

                //Sofia 3/14
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //Sofia 3/11
    public void onUploadImages(View view) {
        Intent intent = new Intent(this, uploadImageActivity.class);
        startActivity(intent);
    }
    public void onImportFiles(View view) {
        Intent intent = new Intent(this, uploadActivity.class);
        startActivity(intent);
    }

    public void onCreateNewImg(View view){
        Intent intent = new Intent(this, DrawingActivity.class);
        startActivity(intent);
    }
}
