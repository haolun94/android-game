package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class MainEditActivity extends AppCompatActivity {
    protected static SingletonData data;
    protected String pageName;
    protected int lastPageIndex;
    protected int gameIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_edit);
        //ADD BY HAOLUN
        data = SingletonData.getInstance(this);
        Intent intent = getIntent();
        lastPageIndex = intent.getIntExtra("lastPageIndex", 0);
        gameIndex = intent.getIntExtra("gameIndex", 0);
        implementSpinner();
        //ADD BY HAOLUN
    }

    @Override
    protected void onResume(){
        super.onResume();
        implementSpinner();
    }

    // when the create new page button is pressed. a new page is created in the database immediately.
    // author: Sofia
    public void onCreateNewPage(View view) {
        lastPageIndex = data.getLastPageIndex(gameIndex);
        int newPageID = data.addNewPage(lastPageIndex, gameIndex);
        Intent intent = new Intent(this,editPageActivity.class);
        intent.putExtra("selectedPageID", newPageID);
        intent.putExtra("gameIndex", gameIndex);
        intent.putExtra("oldPageName", "page" + String.valueOf(lastPageIndex+1));
        startActivity(intent);
    }
    //todo: update the spinner when user comes back to this page --Sofia

    //ADD BY HAOLUN
    public void implementSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.page_spinner);
        List<String> pageList = data.getPageList(gameIndex);
        //List<String> pageList = new ArrayList<String>(pageSet);
        String[] spinnerContent = new String[pageList.size()];
        spinnerContent = pageList.toArray(spinnerContent);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pageName = parent.getItemAtPosition(position).toString();
                //selectPage = getID(name);
                //Toast.makeText(parent.getContext(), "You selected "+ pageName, Toast.LENGTH_SHORT).show();
                //--Sofia 4/13
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    //ADD BY HAOLUN
    public void onApply(View view) {
        int selectedPageID = data.getPageID(pageName, gameIndex);
        if (selectedPageID < 0) return; // todo: throw error
        Intent intent = new Intent(this, editPageActivity.class);
        intent.putExtra("selectedPageID", selectedPageID);
        intent.putExtra("gameIndex", gameIndex);
        intent.putExtra("oldPageName", pageName);
        startActivity(intent);
    }

    public void onDeletePage(View view) {
        int selectedPageID = data.getPageID(pageName, gameIndex);
        data.deletePage(selectedPageID);
        implementSpinner();
    }

}
