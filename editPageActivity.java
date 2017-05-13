package edu.stanford.cs108.bunnyworld;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class editPageActivity extends AppCompatActivity {
    //protected static int selectedPageID;
    protected int selectedPageID;// will not be static anymore because it will be passed to intent, which is safer.
    protected static String selectedImageName; // this is the selected image name in the Image Name spinner. Change when spinner event is triggered.
    protected String shapeText;
    protected String shapeScript;
    protected List<String> pageNameList;
    protected List<String> shapeNameList;
    protected int gameIndex;
    private List<String> imageNameList;
    protected String oldPageName; // The old page name.
    //private static String selectedTrigger, selectedVerb, selectedActionModifier, selectedTriggerModifier; --sofia 3/11

    private SingletonData data;// every activity has its own copy of singletondata. So make it private --Sofia
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);
        data = SingletonData.getInstance(this);

        // Haolun
        Intent intent = getIntent();
        selectedPageID = intent.getIntExtra("selectedPageID", 1); // the default is 1.
        gameIndex = intent.getIntExtra("gameIndex", 1);
        oldPageName = intent.getStringExtra("oldPageName");
        EditText editPageName = (EditText) findViewById(R.id.page_name_edit);
        editPageName.setText(oldPageName); // display default selected Page ID when the activity starts

        //init spinners on the edit page activity
        imageNameList = new ArrayList<>();
        imageNameList.add(Shape.DEFAULT_IMAGENAME);
        imageNameList.addAll(data.getImageNameList());
        implementImageNameSpinner(imageNameList);// get all images from the preload images from singleton
    }

    //callback function when user click the APPLY button on the page-editing activity.
    // When button is clicked, two things are gonna happen:
    // first update the pageName in the pages table and second update the properties of a highlighted shape (if any)
    // author: Sofia
    public  void onApply_editPageAct(View view) {
        EditText editPageName = (EditText) findViewById(R.id.page_name_edit);
        String pageNameStr = editPageName.getText().toString().toLowerCase();
        String currPageName = data.getPageName(selectedPageID);
        List<String> pageNameList = data.getPageNameList(gameIndex);
        if (!pageNameStr.isEmpty() && !pageNameStr.equals(currPageName)) {// if empty page name or page name unchanged, simply ignore it.
            if (currPageName.equals("page1")) {
                Toast.makeText(this, "page1 cannot be renamed",
                        Toast.LENGTH_LONG).show();
                editPageName.setText(currPageName);
            } else if (pageNameList.contains(pageNameStr)) {
                Toast.makeText(this, pageNameStr + "already exists in this game",
                        Toast.LENGTH_LONG).show();
                editPageName.setText(currPageName);
            } else {
                data.setPageName(pageNameStr, selectedPageID);
                // update all the scripts that contain this page
                data.updateScriptPage(currPageName, pageNameStr, gameIndex);
            }
        }
        CustomView cw = (CustomView) findViewById(R.id.custom_view);
        cw.updateHighlightedShape();

    }

    /* Callback func to add a new action to an exiting clause. In the edit field, if the string ends
    * with a semicolon,
    Note: this will only have impact on the editText,
   * it will NOT take affect to the database until the APPLY button is clicked.
   * author: Sofia*/
    public void onAddClause(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_script);
        dialog.setTitle("Add a new clause");
        implementTriggerSpinner(Shape.SCRIPT_TRIGGERS, dialog);// the spinners have to be initialized AFTER dialog has been created.
        implementActionVerbSpinner(Shape.SCRIPT_ACTION_VERBS, dialog);
        Button addButton = (Button) dialog.findViewById(R.id.add_action);
        addButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Spinner triggerSpinner = (Spinner) dialog.findViewById(R.id.dialog_script_trigger_spinner);
                Spinner triggerModifierSpinner = (Spinner) dialog.findViewById(R.id.dialog_script_trigger_modifier_spinner);
                Spinner actionVerbSpinner = (Spinner) dialog.findViewById(R.id.dialog_script_action_verb_spinner);
                Spinner actionModifierSpinner = (Spinner) dialog.findViewById(R.id.dialog_script_action_modifier_spinner);

                String newScript = "";
                EditText editScript = (EditText) dialog.findViewById(R.id.dialog_edit);
                if (editScript.getText().toString().isEmpty()) {// if the field is empty, or the trigger is different from the old one, reset the trigger.
                    if (triggerSpinner.getSelectedItem() != null) newScript += triggerSpinner.getSelectedItem().toString() + " ";
                    if (triggerModifierSpinner.getSelectedItem() != null) newScript += triggerModifierSpinner.getSelectedItem().toString() + " ";
                }
                // add the action parts.
                if (actionVerbSpinner.getSelectedItem() != null) newScript += actionVerbSpinner.getSelectedItem().toString() + " ";
                if (actionModifierSpinner.getSelectedItem() != null) newScript += actionModifierSpinner.getSelectedItem().toString();
                editScript.setText(editScript.getText().toString() + newScript + " ");
            }
        });

        // set the custom dialog components - text, image and button
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editClause = (EditText) dialog.findViewById(R.id.dialog_edit);
                EditText editScript = (EditText) findViewById(R.id.script_edit);
                String newClause = editClause.getText().toString();
                if (!newClause.isEmpty()) {
                    newClause = newClause.substring(0,newClause.length()-1)+";";
                }
                editScript.setText(editScript.getText().toString() + newClause);
                dialog.dismiss();
            }
        });// dialogButton.setOnClickListener
        dialog.show();
    }

    //ADD BY HAOLUN
    public void implementImageNameSpinner(List<String> imageNameList) {
        Spinner spinner = (Spinner) findViewById(R.id.image_name_spinner);
        String[] spinnerContent = new String[imageNameList.size()];
        spinnerContent = imageNameList.toArray(spinnerContent);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(Shape.DEFAULT_IMAGENAME));// set the default name as the currently selected value unless the user changes it.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedImageName = parent.getItemAtPosition(position).toString();//return null if nothing selected
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // initialized in onCreate
    // author: Sofia
    public void implementTriggerSpinner(List<String> triggers, final Dialog dialog) {
        Spinner spinner = (Spinner) dialog.findViewById(R.id.dialog_script_trigger_spinner);
        String[] spinnerContent = new String[triggers.size()];
        spinnerContent = triggers.toArray(spinnerContent);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            // the trigger modifier spinner is updated when an item is selected in trigger spinner
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTrigger = parent.getItemAtPosition(position).toString();//return null if nothing selected
                implementTriggerModifierSpinner(selectedTrigger, dialog);
                EditText editScript = (EditText) dialog.findViewById(R.id.dialog_edit);
                editScript.setText("");// if the user chooses a different trigger, force the current text field to empty because only one trigger can be used.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
     /** trigger modifier spinner menu items are dependent on the trigger spinner. This spinner is updated
      when an item is selected in the trigger spinner. */
     //author: Sofia
    public void implementTriggerModifierSpinner(final String selectedTrigger, final Dialog dialog) {
        Spinner spinner = (Spinner) dialog.findViewById(R.id.dialog_script_trigger_modifier_spinner);
        String[] spinnerContent;
        List<String> spinnerList = null;
        if (selectedTrigger.equals("on drop")) {
            spinnerList = data.getShapeNameList(gameIndex);// get the list from database to prevent any update.
        }

        if (spinnerList == null)
            spinnerContent = new String[0];
        else
            spinnerContent = spinnerList.toArray(new String[spinnerList.size()]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//               selectedTriggerModifier = parent.getItemAtPosition(position).toString();//return null if nothing selected
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
    }


    //initizlized in onCreate
    //author: Sofia
    public void implementActionVerbSpinner(List<String> actionVerbs, final Dialog dialog) {
        Spinner spinner = (Spinner) dialog.findViewById(R.id.dialog_script_action_verb_spinner);
        String[] spinnerContent = actionVerbs.toArray(new String[actionVerbs.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVerb = parent.getItemAtPosition(position).toString();//return null if nothing selected
                implementActionModifierSpinner(selectedVerb, dialog);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // the action modifier spinner. This is dependent on action verb spinner
    //author: Sofia
    public void implementActionModifierSpinner(String selectedVerb, Dialog dialog) {
        Spinner spinner = (Spinner) dialog.findViewById(R.id.dialog_script_action_modifier_spinner);
        String[] spinnerContent;
        List<String> spinnerList;
        if (selectedVerb.equals("goto")) {// goto <page-name>
            // get the page List only right before making the spinner list because sometimes user will change page name on the fly
            spinnerList = data.getPageList(gameIndex);
            //spinnerList = pageNameList;
        } else if (selectedVerb.equals("play")) {
            spinnerList = data.getSoundNameList();
        } else {
            // Only get the shape list here. Same idea as pageNameList
            spinnerList = data.getShapeNameList(gameIndex);
            // spinnerList = shapeNameList;
        }
        spinnerContent = spinnerList.toArray(new String[spinnerList.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerContent);
        spinner.setAdapter(adapter);

    }

    public void onCopy(View view){


        CustomView cw = (CustomView) findViewById(R.id.custom_view);
        cw.copyShape();

    }

    public void onPaste(View view){
        CustomView cw = (CustomView) findViewById(R.id.custom_view);

        cw.pasteShape();
        cw.invalidate();
    }

}
