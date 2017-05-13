package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sofiazhang on 3/6/17.
 * This CustomView is the canvas shown in editPageActivity.
 * The view displays current page. The user can create/edit shapes on the canvas.
 */


public class CustomView extends View {
    private final int pageID;// do not ever change the pageID once initialized
    private Context context;
    List<Integer> shapeIDs;
    List<Shape> shapeList;// a list of shapes on the current page. This is from database
    Set<String> shapeNameSet;// a set of unique shape names to prevent user from creating a shape name that already exists.
    private static SingletonData data;
    private int gameIndex;
    private boolean isScaleImage;
    protected static int copiedShapeID = -1;
    protected static Shape copiedShape;
    private final float CANVAS_LEFT = 0;
    private final float CANVAS_RIGHT = 1000;
    private final float CANVAS_TOP = 0;
    private final float CANVAS_BOTTOM = 450;

    protected static final String DEFAULT_FONTSIZE= "30";
    protected static final String DEFAULT_FONTCOLOR="BLACK";
    protected static final int MAX_FONTSIZE = 200;
//    EditText editPageName, editLeft, editTop, editRight, editBottom, editText, editFont, editShapeName, editScript;
//    CheckBox checkMovable, checkVisible;
    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        data = SingletonData.getInstance(context);
        Intent intent = ((Activity)getContext()).getIntent();
        pageID = intent.getIntExtra("selectedPageID", 1);
        gameIndex = intent.getIntExtra("gameIndex", 1);
        // now that we have the pageID, we can retrieve all the shapeIDs given the pageID
        shapeList = new ArrayList<>();
        List<String> shapeNameList = data.getShapeNameList();
        shapeNameSet = new HashSet<>(shapeNameList);
        getShapeListFromPage(pageID);
        //getShapeListFromPage(editPageActivity.selectedPageID);
        // initialize all the property fields
    }

    /* Get a list of all shapes in the current page given the page ID.
    * get all the shapes given the page ID. Shape properties are populated by constructor :)
    * */
    private void getShapeListFromPage(int pageID) {
        shapeIDs = data.getShapesFromPage(pageID);
        shapeList = new ArrayList<>();
        for (int id : shapeIDs) {
            Shape shape = new Shape(id, data);
            shapeList.add(shape);
        }
    }
    public void drawBoundingBox(Canvas canvas) {
        Paint outlinePaint;
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(CANVAS_LEFT, CANVAS_TOP, CANVAS_RIGHT, CANVAS_BOTTOM, outlinePaint);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoundingBox(canvas);
        for (Shape s : shapeList) {
            s.drawShape(canvas, context);// draw all the shapes
        }
    }


    /* update the fields of the currently highlighted Shape Object and push these updates to the database.
    * Usage: This function is called when the APPLY button on editPageActivity is clicked.
    * All the current changes will be saved in Shape class and in database.
    * If currently there is no shape highlighted at all, then there won't be any change to any shape or the database.
    * */
    public void updateHighlightedShape() {
        EditText editLeft = (EditText) ((Activity) getContext()).findViewById(R.id.left_edit);
        EditText editTop = (EditText) ((Activity) getContext()).findViewById(R.id.top_edit);
        EditText editRight = (EditText) ((Activity) getContext()).findViewById(R.id.right_edit);
        EditText editBottom = (EditText) ((Activity) getContext()).findViewById(R.id.bottom_edit);
        CheckBox checkMovable = (CheckBox) ((Activity) getContext()).findViewById(R.id.movable);
        CheckBox checkVisible = (CheckBox) ((Activity) getContext()).findViewById(R.id.visible);
        EditText editText = (EditText) ((Activity) getContext()).findViewById(R.id.text_edit);
        CheckBox checkBold =  (CheckBox) ((Activity) getContext()).findViewById(R.id.bold);
        CheckBox checkItalic =  (CheckBox) ((Activity) getContext()).findViewById(R.id.italic);
        EditText editShapeName = (EditText) ((Activity) getContext()).findViewById(R.id.shape_name);
        EditText editScript = (EditText) ((Activity) getContext()).findViewById(R.id.script_edit);

        for (Shape s : shapeList) {
            if (s.isHighlighted()) {
                //String imageName = editPageActivity.selectedImageName;// this is from the Imagename spinner
                Spinner imageNameSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.image_name_spinner);
                String imageName = imageNameSpinner.getSelectedItem().toString();

                String left = editLeft.getText().toString();
                String top = editTop.getText().toString();
                String right = editRight.getText().toString();
                String bottom = editBottom.getText().toString();
                // update the geometry only if the inputs are correct. Otherwise keep the previous geometry
                //if the geometry fields are empty, ignore them. keep the original geometry.
                if (isValidGeometry(left, top, right, bottom)) {
                    String geometry = editLeft.getText().toString() + " "
                            + editTop.getText().toString() + " "
                            + editRight.getText().toString() + " "
                            + editBottom.getText().toString();
                    s.setGeometry(geometry);
                }else{
                    dialogBox("Invalid geometry input. Geometry unchanged.");
                }

                boolean isVisible = checkVisible.isChecked();
                boolean isMovable = checkMovable.isChecked();
                String text = editText.getText().toString();

                String fontSizeStr = generateFontSize();

                String boldStr = checkBold.isChecked()?  "1" : "0";
                String ItalicStr = checkItalic.isChecked()? "1" : "0";
                String fontColorStr = generateFontColor();

                String fontStyle = boldStr+" "+ItalicStr+" "+fontColorStr+" "+fontSizeStr;

                if (!fontStyle.isEmpty()) {
                    s.setFontStyle(fontStyle);
                }

                String shapeName = editShapeName.getText().toString().toLowerCase();
                // if shapeName field is empty, ignore it.
                // Update the script for the shapes that involves the new name.
                if (!shapeName.equals(s.getShapeName()) && !shapeName.isEmpty()) {
                    if (shapeNameSet.contains(shapeName)) {
                        //((Activity)getContext()).dialogBox("a");
                        dialogBox("This shape name is already in use");
                    } else if(shapeName.equals("drop")
                            || shapeName.equals("hide")|| shapeName.equals("show")
                            || shapeName.equals("goto")){
                        dialogBox("Illegal shape name.");
                    } else{
                        data.updateScriptShape(s.getShapeName(), shapeName);
                        shapeNameSet.remove(s.getShapeName());// remove the old shape from the set
                        s.setShapeName(shapeName);
                        shapeNameSet.add(shapeName);// add the new shape to the set
                    }

                }
                s.setImageName(imageName);
                s.setMovable(isMovable);
                s.setVisible(isVisible);
                s.setText(text);

                // Update the script for the specific shape. See if the script has been changed
                // by the user. If not, then retrieve the updated script from database.
                String script = editScript.getText().toString().toLowerCase();
                String oldScript = s.getScript();
                if(!script.equals(oldScript)){
                    if (proofreadScript(script)) {
                        s.setScript(script);
                    }
                }else{
                    script = data.getScript(s.getShapeID());
                    editScript.setText(script);
                    s.setScript(script);
                }
                data.updateShapeOnSelect(s);//push updates to database
                //soifa -- 3/11
//                updateActionModifierSpinner(editPageActivity.selectedVerb);// update the spinner
//                updateTriggerModifierSpinner(editPageActivity.selectedTrigger);
                invalidate();
                Toast.makeText(this.getContext(), "shape properties updated", Toast.LENGTH_LONG).show();

                break;
            }
        }
        getShapeListFromPage(pageID);

    }
    // Check if the input geometry is valid.
    private boolean isValidGeometry(String left, String top, String right, String bottom){
        float l = Float.parseFloat(left);
        float t = Float.parseFloat(top);
        float r = Float.parseFloat(right);
        float b = Float.parseFloat(bottom);
        if(!left.isEmpty()  && l >= 0
                && !top.isEmpty() && t >= 0
                && !right.isEmpty() && r >= 0
                && !bottom.isEmpty() && b >= 0){
            if(l < r && t < b){
                return inBound(l, t, r, b);
            }

        }
        return false;
    }

    /**********************************************************************************************
     *
     *                        ON TOUCH EVENT HANDLING
     *
     *********************************************************************************************/

    float x1,y1,x2,y2;
    /* Ontouch Event handler at creating new shape mode
    * A new shape is created and added to the database when the user drag on the canvas.
    * This shape is highlighted at creation time. */
    private final float SCROLL_THRESHOLD = 20;
    private void createNewShapeHandler(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                if ((Math.abs(x1 - x2) > SCROLL_THRESHOLD || Math.abs(y1 - y2) > SCROLL_THRESHOLD)) {// ignore if this is a click event. shape is created only if it is a drag event
                    //String imageName = editPageActivity.selectedImageName;
                    // Do not get imageName from activity static var. Get it from spinner object, which is safer.
                    Spinner imageNameSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.image_name_spinner);
                    String imageName = imageNameSpinner.getSelectedItem().toString();
                    CheckBox checkScaleImage = (CheckBox) ((Activity) getContext()).findViewById(R.id.scale_image);
                    adjustGeometry();
                    isScaleImage = checkScaleImage.isChecked();
                    if (isScaleImage) {
                        Bitmap bitmap = data.getBitmap(imageName);
                        if (bitmap != null) {
                            if (Math.abs(x1 - x2) / Math.abs(y1 - y2) < ((float) bitmap.getWidth()) / bitmap.getHeight()) {
                                y2 = y1 + Math.abs(x1 - x2) * bitmap.getHeight() / bitmap.getWidth() * (y2 - y1) / Math.abs(y2 - y1);
                            } else {
                                x2 = x1 + Math.abs(y1 - y2) * bitmap.getWidth() / bitmap.getHeight() * (x2 - x1) / Math.abs(x2 - x1);
                            }
                        }
                    }
                    String geometry = Shape.coordinatesToGeometry(x1, y1, x2, y2);
                    int currShapeID = data.createNewShapeOnDrag(pageID, imageName, geometry);
                    EditText editText = (EditText) ((Activity) getContext()).findViewById(R.id.text_edit);

                    CheckBox checkBold =  (CheckBox) ((Activity) getContext()).findViewById(R.id.bold);
                    CheckBox checkItalic =  (CheckBox) ((Activity) getContext()).findViewById(R.id.italic);
                    Shape curr = new Shape(currShapeID, data);
                    String fontSizeStr = generateFontSize();
                    String fontColorStr = generateFontColor();
                    String boldStr = checkBold.isChecked() ? "1" : "0";
                    String ItalicStr = checkItalic.isChecked() ? "1" : "0";
                    String fontStyle = boldStr+" "+ItalicStr+" "+fontColorStr+" "+fontSizeStr;
                    curr.setFontStyle(fontStyle);


                    if (!editText.getText().toString().equals("")) {
                        curr.setText(editText.getText().toString());

                    }
                    data.updateShapeOnSelect(curr);
                    unHighlightAll();
                    curr.highlight(); // immediately high when the shape is just created
                    shapeList.add(curr);// add the current shape to the list
                    shapeNameSet.add(curr.getShapeName());// add shape to the shape name set

                    updateFieldsInfo(curr);// display the properties of this new shape
                    invalidate(); // redraw on canvas
                }
        }
    }
    // Used in create on drag to prevent the shape being out of bound.
    private void adjustGeometry(){
//        x1 = Math.max(x1, CANVAS_LEFT);
//        y1 = Math.min(y1, CANVAS_TOP);
        x2 = Math.min(x2, CANVAS_RIGHT);
        y2 = Math.min(y2, CANVAS_BOTTOM);
    }
    // Generate the font size (also check if it is legal in the meantime)
    private String generateFontSize(){
        EditText editFontSize = (EditText) ((Activity) getContext()).findViewById(R.id.font_size);
        String fontSizeStr = DEFAULT_FONTSIZE;
        String input = editFontSize.getText().toString();
        if(!input.equals("")){
            if(Integer.parseInt(input) < MAX_FONTSIZE){
                fontSizeStr = input;
            }else{
                dialogBox("Invalid font size. Font size has been set to default.");
            }
        }
        return fontSizeStr;
    }
    // Generate the font color (also check if it is legal in the meantime)
    private String generateFontColor(){
        EditText editFontColor = (EditText) ((Activity) getContext()).findViewById(R.id.font_color);
        String fontColorStr = DEFAULT_FONTCOLOR;
        String input = editFontColor.getText().toString().toUpperCase();
        if(!input.equals("")){
            if(Shape.checkFontColor(input)){
                fontColorStr = input;
            }else{
                dialogBox("Invalid color. Color has been set to default.");
            }

        }
        return fontColorStr;
    }

    /* Ontouch event handler at select a shape mode.
    * The selected shape will be highlighted. And all the property fields are updated to display
    * the properties of the currently selected shape
    * */

    Shape curr = null;
    private float preLeft, preTop, preRight, preBottom;
    private float x_click, y_click, x_move, y_move;// position of the mouse click
    private void selectShapeHandler(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {// down , up, move
            x_click = event.getX();
            y_click = event.getY();// this is where user tapped
            curr = searchShape(x_click, y_click);
            if (curr == null) {
                unHighlightAll();
                clearAllFields();

            } else {
                unHighlightAll();
                curr.highlight();
                preLeft = curr.getLeft();
                preTop = curr.getTop();
                preRight = curr.getRight();
                preBottom = curr.getBottom();
                //updateFieldsInfo(curr);
            }
            invalidate();//
        } else if(event.getAction() == MotionEvent.ACTION_UP){
            if (curr != null) {
                float newLeft = curr.getLeft();
                float newTop = curr.getTop();
                float newRight = curr.getRight();
                float newBottom = curr.getBottom();
                if (!inBound(newLeft, newTop, newRight, newBottom)) {
                    if (newLeft < CANVAS_LEFT) {
                        newRight += CANVAS_LEFT - newLeft; //offset
                        newLeft = CANVAS_LEFT;

                    }
                    if (newRight > CANVAS_RIGHT) {
                        newLeft -= newRight - CANVAS_RIGHT;
                        newRight = CANVAS_RIGHT;

                    }
                    if (newTop < CANVAS_TOP) {
                        newBottom += CANVAS_TOP - newTop;
                        newTop = CANVAS_TOP;

                    }
                    if (newBottom > CANVAS_BOTTOM) {
                        newTop -= newBottom - CANVAS_BOTTOM;
                        newBottom = CANVAS_BOTTOM;

                    }
                }
                curr.setGeometry(newLeft, newTop, newRight, newBottom);
                updateFieldsInfo(curr);
            }
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // Action move event: First one is implementing the dragging, which constantly set the
            // geometry of the aboveshape. The second part is dealing with the under shape which
            // has the on drop trigger, highlight it when the above shape is above it and unhighlight
            // it when the aboveshape is away.

            x_move = event.getX();
            y_move = event.getY();
            if (curr != null) {
                //drag
                float xMove = x_move - x_click;
                float yMove = y_move - y_click;
                float newLeft = preLeft + xMove;
                float newTop = preTop + yMove;
                float newRight = preRight + xMove;
                float newBottom = preBottom + yMove;
                curr.setGeometry(newLeft, newTop, newRight, newBottom);
            }
            invalidate();
        }
    }//end of selectShapeHandler



    /* Ontouch event handler at erase a shape mode.
    * The selected shape will be deleted from both the shape list and the database */
    private void eraseShapeHandler(MotionEvent event) {
        float x,y; //position of the mouse click
        if (event.getAction() == MotionEvent.ACTION_DOWN) {// down, up
            x = event.getX();
            y = event.getY();// this is where user tapped
            eraseShape(x, y);
            clearAllFields();
            invalidate();//
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        RadioGroup radioGroup = (RadioGroup) ((Activity)getContext()).findViewById(R.id.selection_mode);
        int radioChecked = radioGroup.getCheckedRadioButtonId();
        if (radioChecked == R.id.select_shape) {
            selectShapeHandler(event);
        } else if (radioChecked == R.id.erase_shape) {
            eraseShapeHandler(event);
        } else {
           createNewShapeHandler(event);
        }
            return true;
    }
    /**************************************************************************************************
     *
     *                   HELPER FUNCTIONS
     *
     **************************************************************************************************/

    private boolean inBound(float left, float top, float right, float bottom) {
        return (left >= CANVAS_LEFT && right <= CANVAS_RIGHT && top >= CANVAS_TOP && bottom <= CANVAS_BOTTOM);
    }

    /* Search the most recently created shape that contains the point x and y*/
    private Shape searchShape(float x, float y) {
        int ct = shapeList.size()-1;
        while (ct >= 0) {
            Shape curr = shapeList.get(ct);
            if (curr.containsPoint(x,y)) {
                return curr;
            }
            ct--;
        }
        return null;
    }
    /* Unlightlight all the shapes when the user click somewhere else but a shape */
    private void unHighlightAll() {
        for (int i = 0; i < shapeList.size(); i++) {
            shapeList.get(i).unHighlight();
        }
    }

    /*Fill in all the property fields of the currently selected Shape */
    private void updateFieldsInfo(Shape curr) {
        //todo: set the selected item of the image Name inside spinner. Need to find a way to grab spinner
        //todo: may also change the item inside script spinners. Not sure if this is necessary tho

        EditText editLeft = (EditText) ((Activity) getContext()).findViewById(R.id.left_edit);
        EditText editTop = (EditText) ((Activity) getContext()).findViewById(R.id.top_edit);
        EditText editRight = (EditText) ((Activity) getContext()).findViewById(R.id.right_edit);
        EditText editBottom = (EditText) ((Activity) getContext()).findViewById(R.id.bottom_edit);
        CheckBox checkMovable = (CheckBox) ((Activity) getContext()).findViewById(R.id.movable);
        CheckBox checkVisible = (CheckBox) ((Activity) getContext()).findViewById(R.id.visible);
        CheckBox checkScaleImage = (CheckBox) ((Activity) getContext()).findViewById(R.id.scale_image);
        EditText editText = (EditText) ((Activity) getContext()).findViewById(R.id.text_edit);

        EditText editFontSize = (EditText) ((Activity) getContext()).findViewById(R.id.font_size);
        EditText editFontColor = (EditText) ((Activity) getContext()).findViewById(R.id.font_color);
        CheckBox checkBold =  (CheckBox) ((Activity) getContext()).findViewById(R.id.bold);
        CheckBox checkItalic =  (CheckBox) ((Activity) getContext()).findViewById(R.id.italic);

        EditText editShapeName = (EditText) ((Activity) getContext()).findViewById(R.id.shape_name);
        EditText editScript = (EditText) ((Activity) getContext()).findViewById(R.id.script_edit);
        Spinner imageNameSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.image_name_spinner);
        ArrayAdapter arrayAdapter = (ArrayAdapter) imageNameSpinner.getAdapter();

        editLeft.setText(String.format("%.2f", curr.getLeft()));
        editTop.setText(String.format("%.2f", curr.getTop()));
        editRight.setText(String.format("%.2f", curr.getRight()));
        editBottom.setText(String.format("%.2f", curr.getBottom()));
        checkMovable.setChecked(curr.getMovable());
        checkVisible.setChecked(curr.getVisible());
        checkScaleImage.setChecked(false);
        editText.setText(curr.getText());

        String curFontStyle = curr.getFontStyle();
        String[] tokens = curFontStyle.split("\\s+");
        if(tokens[0].equals("0")){
            checkBold.setChecked(false);
        }else{
            checkBold.setChecked(true);
        }

        if(tokens[1].equals("0")){
            checkItalic.setChecked(false);
        }else{
            checkItalic.setChecked(true);
        }

        editFontColor.setText(tokens[2]);
        editFontSize.setText(tokens[3]);
        editShapeName.setText(curr.getShapeName());
        editScript.setText(curr.getScript());
        imageNameSpinner.setSelection(arrayAdapter.getPosition(curr.getImageName()));

    }

    /*Clear all the property fields */
    private void clearAllFields() {

        EditText editLeft = (EditText) ((Activity) getContext()).findViewById(R.id.left_edit);
        EditText editTop = (EditText) ((Activity) getContext()).findViewById(R.id.top_edit);
        EditText editRight = (EditText) ((Activity) getContext()).findViewById(R.id.right_edit);
        EditText editBottom = (EditText) ((Activity) getContext()).findViewById(R.id.bottom_edit);
        CheckBox checkMovable = (CheckBox) ((Activity) getContext()).findViewById(R.id.movable);
        CheckBox checkVisible = (CheckBox) ((Activity) getContext()).findViewById(R.id.visible);
        CheckBox checkScaleImage = (CheckBox) ((Activity) getContext()).findViewById(R.id.scale_image);

        EditText editText = (EditText) ((Activity) getContext()).findViewById(R.id.text_edit);
        EditText editFontSize = (EditText) ((Activity) getContext()).findViewById(R.id.font_size);
        EditText editFontColor = (EditText) ((Activity) getContext()).findViewById(R.id.font_color);
        CheckBox checkBold =  (CheckBox) ((Activity) getContext()).findViewById(R.id.bold);
        CheckBox checkItalic =  (CheckBox) ((Activity) getContext()).findViewById(R.id.italic);

        EditText editShapeName = (EditText) ((Activity) getContext()).findViewById(R.id.shape_name);
        EditText editScript = (EditText) ((Activity) getContext()).findViewById(R.id.script_edit);
        Spinner imageNameSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.image_name_spinner);
        ArrayAdapter arrayAdapter = (ArrayAdapter) imageNameSpinner.getAdapter();

        editLeft.setText("");
        editTop.setText("");
        editRight.setText("");
        editBottom.setText("");

        checkMovable.setChecked(false);
        checkVisible.setChecked(false);
        checkScaleImage.setClickable(true);
        checkScaleImage.setChecked(false);

        editText.setText("");
        editFontSize.setText("");
        editFontColor.setText("");
        checkBold.setChecked(false);
        checkItalic.setChecked(false);


        editShapeName.setText("");
        editScript.setText("");
        imageNameSpinner.setSelection(arrayAdapter.getPosition(Shape.DEFAULT_IMAGENAME));
    }

    // erase the most recently created shape that contains the point x and y
    void eraseShape(float x, float y) {
        for (int i = shapeList.size()-1; i >=0; i--) {
            Shape curr = shapeList.get(i);
            if (curr.containsPoint(x,y)) {
                shapeList.remove(i);
                shapeNameSet.remove(curr.getShapeName());
                data.deleteShapeOnErase(curr.getShapeID());
                return;
            }
        }
    }


    public void dialogBox(String alertmessage)  {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)getContext());
        builder.setTitle("Alert");
        builder.setMessage(alertmessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // You don't have to do anything here if you just want it dismissed when clicked
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        // Create the AlertDialog object and return it
    }

	 public void copyShape(){
         for(Shape s : shapeList){
             if(s.isHighlighted()){
                 int shapeId = s.getShapeID();
                 copiedShape = new Shape(shapeId,data);
                 Toast.makeText(this.getContext(), "Shape copied", Toast.LENGTH_LONG).show();
                 break;
             }
         }

     }
	  public void pasteShape(){
          if(copiedShape != null){
              shapeList.add(copiedShape);
              copiedShape.setCopyParams(pageID);
              String query = copiedShape.addQuery(String.valueOf(pageID));
              data.execQuery(query);
             copiedShape = null;
          }else{
              Toast.makeText(this.getContext(), "nothing to paste", Toast.LENGTH_LONG).show();
          }

     }



    /**************************************************************************************************
     *
     *                   CHECK SCRIPT CORRECTNESS
     *
     **************************************************************************************************/

    private List<String> soundNameList, pageNameList, shapeNameInGame;

    public boolean checkClauseActions(String str) {
        //base case
        if (str.isEmpty()) return true;
        if (str.startsWith("goto ")) {
            String prefix = str.substring(5);// "goto " has 5 chars
            int ct = 0;
            while (ct < pageNameList.size()) {
                String s = pageNameList.get(ct);
                if (prefix.startsWith(s)) {
                    return prefix.length() > s.length() ? checkClauseActions(prefix.substring(s.length()+1)) : checkClauseActions(prefix.substring(s.length()));
                }
                ct++;
            }
            if (ct == pageNameList.size()) {
                return false;
            }

        } else if (str.startsWith("play ")) {
            String prefix = str.substring(5);// "play " has 5 chars
            int ct = 0;
            while (ct < soundNameList.size()) {
                String s = soundNameList.get(ct);
                if (prefix.startsWith(s)) {
                    return prefix.length() > s.length() ? checkClauseActions(prefix.substring(s.length()+1)) : checkClauseActions(prefix.substring(s.length()));
                }
                ct++;
            }
            if (ct == soundNameList.size()) {
                return false;
            }

        } else if (str.startsWith("hide ")) {
            String prefix = str.substring(5);// "hide " and "show" both have 5 chars
            int ct = 0;
            while (ct < shapeNameInGame.size()) {
                String s = shapeNameInGame.get(ct);
                if (prefix.startsWith(s)) {
                    return prefix.length() > s.length() ? checkClauseActions(prefix.substring(s.length()+1)) : checkClauseActions(prefix.substring(s.length()));
                }
                ct++;
            }
            if (ct == shapeNameInGame.size()) {
                return false;
            }

        } else if (str.startsWith("show ")) {
            String prefix = str.substring(5);// "play " has 5 chars
            int ct = 0;
            while (ct < shapeNameInGame.size()) {
                String s = shapeNameInGame.get(ct);
                if (prefix.startsWith(s)) {
                    return prefix.length() > s.length() ? checkClauseActions(prefix.substring(s.length()+1)) : checkClauseActions(prefix.substring(s.length()));
                }
                ct++;
            }
            if (ct == shapeNameInGame.size()) {
                return false;
            }
        }
        return false;
    }


    public boolean proofreadScript(String script) {
        soundNameList = data.getSoundNameList();
        pageNameList = data.getPageNameList(gameIndex);
        shapeNameInGame = data.getShapeNameList(gameIndex);

        script.toLowerCase();
        if (script.isEmpty()) return true;
        String[] clauses = script.split(";");// semicolon will be removed after split
        if (clauses.length == 0) {
            dialogBox("Missing semicolon in the script.");
            return false;
        }
        for(String clause : clauses){
            if (clause.startsWith("on click ")) {
                String trigger = new String("on click ");
                String substr = clause.substring(trigger.length());
                if (!checkClauseActions(substr)){
                    dialogBox("script actions incorrect.");
                    return false;
                }
            } else if (clause.startsWith("on enter ")) {
                String trigger = new String("on enter ");
                String substr = clause.substring(trigger.length());
                if (!checkClauseActions(substr)){
                    dialogBox("script actions incorrect.");
                    return false;
                }

            } else if (clause.startsWith("on drop ")) {
                String trigger = new String("on drop ");
                String substr = clause.substring(trigger.length());
                int ct = 0;
                while (ct < shapeNameInGame.size()) {
                    String s = shapeNameInGame.get(ct)+ " ";
                    if (substr.startsWith(s)) {
                        if (!checkClauseActions(substr.substring(s.length()))){
                            dialogBox("script actions incorrect.");
                            return false;
                        }
                        break;
                    }
                    ct++;
                }
                if (ct == shapeNameInGame.size()) {
                    dialogBox("Missing a shape name after 'on drop'. Use 'on drop <shape-name> <actions>'.");
                    return false;
                }
            } else {
                dialogBox("script trigger is incorrect.");
                return false;
            }
        }
        return true;

    }



}//end of class


