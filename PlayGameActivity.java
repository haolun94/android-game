package edu.stanford.cs108.bunnyworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class PlayGameActivity extends AppCompatActivity {
    //protected static String selectedBackground;
    protected static String DEFAULTBG = "--";
    SingletonData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Page.reset();
        setContentView(R.layout.activity_play_game);
        data = SingletonData.getInstance(this);
        implementImageNameSpinner();
    }

    //ADD BY HAOLUN
    public void implementImageNameSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.background_name_spinner);
        List<String> bgNameList = new ArrayList<>();
        bgNameList.add(DEFAULTBG);
        bgNameList.addAll(data.loadFromAssets(data.BACKGROUND_DIR_ASSETS));
        String[] spinnerContent = new String[bgNameList.size()];
        spinnerContent = bgNameList.toArray(spinnerContent);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        //spinner.setSelection(adapter.getPosition("evening"));
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(DEFAULTBG));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //selectedBackground = parent.getItemAtPosition(position).toString();//return null if nothing selected
                PlayerCustomView pcw = (PlayerCustomView) findViewById(R.id.player_custom_view);
                pcw.setBackground();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
