package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sofiazhang on 3/3/17.
 */

public class Shape{
    protected static final String DEFAULT_FONTSTRING = "0 0 BLACK 30";
    private String shapeName;// the shape name
    private String imageName;
    private boolean movable, visible;
    private String script;
    private String text;
    private String fontStyle;
    private String geometry;
    private float top, bottom, left, right;
    private int pageID; //pageID might be necessary. Used for copy paste -- Haoxuan
    private int shapeID;// shapeID can only be changed when it is used in copy shape
    private boolean isHighlighted; //shape can be highlighted in the custom view
    Paint outlinePaint, fillPaint, textPaint;
    // added by Sofia. It would be so much easier
    // to interact with database directly in Shape class.
    // Otherwise you have to make a long query every single time.
    private SingletonData data;

    static List<String> SCRIPT_ACTION_VERBS = new ArrayList<>(Arrays.asList("goto", "play", "hide", "show"));
    static List<String> SCRIPT_TRIGGERS = new ArrayList<>(Arrays.asList("on click", "on enter", "on drop"));
    static String DEFAULT_IMAGENAME = "--";// if default, draw gray rectangle

    // default constructor. Give the shapeID, retrieve everything from the Shapes table and populate
    // all the variables here.
    public Shape(int shapeID, SingletonData data) {
        this.data = data;
        this.shapeID = shapeID;

        pageID = data.getPageID(shapeID);

        shapeName = data.getShapeName(shapeID);

        imageName = data.getImageName(shapeID);

        movable = data.getMovable(shapeID) > 0 ? true : false;

        visible = data.getVisible(shapeID) > 0 ? true : false;

        script = data.getScript(shapeID);

        text = data.getText(shapeID);

        fontStyle = data.getTextFont(shapeID);

        geometry = data.getShapeGeometry(shapeID);

        parseGeometryStr(geometry);

        // the paint properties of the rectangle outline
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.GREEN);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(5.0f);// normal stroke width

        // the paint properties of the rectangle fill
        fillPaint = new Paint();
        fillPaint.setColor(Color.GRAY);

        // the paint properties of the text
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);

        setFontStyle(fontStyle);

        isHighlighted = false;
    }

    /* parse the geometry string into four floats: left, top, right, bottom
    /  Assume: geometry string is a valid string consisting of four floats.*/
    private void parseGeometryStr(String geometryStr){
        String[] tokens = geometryStr.split("\\s+");
        // The order in database is: left, top, right, bottom
        left = Float.parseFloat(tokens[0]);
        top = Float.parseFloat(tokens[1]);
        right = Float.parseFloat(tokens[2]);
        bottom = Float.parseFloat(tokens[3]);
    }



    /*given two coordinates, convert to a string consisting of four floats representing geometry
    * called when storing the geometry to database.
    * The order in database is: left, top, right, bottom*/
    public static String coordinatesToGeometry(float x1, float y1, float x2, float y2) {
        float local_left = Math.min(x1, x2);
        float local_right = Math.max(x1, x2);
        float local_top = Math.min(y1, y2);
        float local_bottom = Math.max(y1, y2);
        String output = String.format("%.2f",local_left)+" "
                + String.format("%.2f",local_top)+ " "
                + String.format("%.2f",local_right)+ " "
                + String.format("%.2f",local_bottom);
        return output;
    }

    public static boolean checkFontColor(String color){
        try{
            Color.parseColor(color);
            return true;
        }catch (IllegalArgumentException e){

            return false;
        }
    }

    // highlight a shape by making the rectangle border bold and blue and the text bold
    public void highlight() {
//        grayOutlinePaint.setStrokeWidth(15.0f);
//        grayOutlinePaint.setColor(Color.BLUE);
        textPaint.setFakeBoldText(true);
        isHighlighted = true;
    }
    // unhighlight by reverse the paint settings (except for the bold text)
    public void unHighlight() {

        String[] tokens = fontStyle.split("\\s+");
        int bold = Integer.parseInt(tokens[0]);
        if(bold == 0){
            textPaint.setFakeBoldText(false);
        }
        isHighlighted = false;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }
    /* Check if a point clicked by the user is in this shape area.
    * */
    boolean containsPoint(float x, float y) {
        return left <= x && x <= right && top <= y && y <= bottom;
    }


    /******************************************************************************************
     *
     *  Setters and Getters
     *
    ******************************************************************************************/

    public void setMovable(boolean isMovable) {
        movable = isMovable;
    }
    public void setVisible(boolean isVisible) {
        visible = isVisible;
    }
    //overload. given a geometry string: left, top, right, bottom by client, get all four floats.
    public void setGeometry(String geometryStr) {
        parseGeometryStr(geometryStr);
    }
    //overload. unused --sofia
    public void setGeometry(float newLeft, float newTop, float newRight, float newBottom){
        left = newLeft;
        top = newTop;
        right = newRight;
        bottom = newBottom;
    }
    public void setScript(String script){
        this.script = script;
    }
    public void setShapeName(String name){
        // todo:
        // change the script that involves this name.

        String oldName = shapeName;


        shapeName = name;
    }

    public void setText(String text){
        this.text = text;
    }

    public void setFontStyle(String newFontStyle){
        this.fontStyle = newFontStyle;
        String[] tokens = fontStyle.split("\\s+");

        int bold = Integer.parseInt(tokens[0]);
        int italics = Integer.parseInt(tokens[1]);
        int color = Color.parseColor(tokens[2]);
        int newFontSize = Integer.parseInt(tokens[3]);


        textPaint.setTextSize(newFontSize);

        if(bold ==1 && italics ==1){
            textPaint.setFakeBoldText(true);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        }
        if(bold ==1 && italics ==0){
            textPaint.setFakeBoldText(true);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }
        if(bold ==0 && italics ==1){
            textPaint.setFakeBoldText(false);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        }
        if(bold ==0 && italics ==0){
            textPaint.setFakeBoldText(false);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }

        textPaint.setColor(color);
    }
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName(){
        return imageName;
    }
    public boolean getMovable() {return movable;}
    public boolean getVisible() {return visible;}
    public float getLeft() {return left;}
    public float getTop() {return top;}
    public float getRight() {return right;}
    public float getBottom() {return bottom;}
    public String getScript(){
        return script;
    }
    public String getText(){
        return text;
    }
    public String getFontStyle(){
        return fontStyle;
    }
    public String getShapeName(){
        return shapeName;
    }
    public int getShapeID() {return shapeID;}

    // Update the copied shape parameters. Called before the shape is inserted into database
    public void setCopyParams(int newPageId){
        pageID = newPageId;
        Cursor cursor = data.getQueryCursor("SELECT _id FROM shapes");
        int lastID = 0;
        if (cursor.moveToLast()){
            lastID= cursor.getInt(0);
        }
        shapeID = lastID + 1;
        String copyShapeName = "shape" + Integer.toString(lastID + 1);
        setShapeName(copyShapeName);
    }


    // Generate the insert query for this shape;
    // The geometry is in the order of left top right bottom
    public String addQuery(String pageid){
        String geometry = left + " " + top + " " + right + " " + bottom;
        String query = "INSERT INTO shapes VALUES(" + pageid + ", '" + shapeName + "', '" + geometry + "', '";
        query += script + "', '" + imageName + "', " ;
        if(movable){
            query += 1 + ", ";
        }else{
            query += 0 + ", ";
        }
        if(visible){
            query += 1 + ", ";
        }else{
            query += 0 + ", ";
        }
        query +=  "'" + text + "', '" + fontStyle + "', NULL);";
        System.out.println(query);

        return query;

    }




/***************************************************************************************/

// draw the shape on canvas

// modified by by Sofia the
public void drawShape(Canvas canvas, Context context) {
    //--Sofia 3/17
    if(isHighlighted) {
        canvas.drawRect(left, top, right, bottom, outlinePaint);
    }
    if (!text.equals("")){// text takes precedence. If text nonempty, draw text ONLY
        canvas.drawText(text, left, bottom, textPaint);
        return;
    }
    if(isHighlighted) {
        canvas.drawRect(left, top, right, bottom, outlinePaint);
    }
    if (imageName.equals(DEFAULT_IMAGENAME)) {// if default, draw gray rectangle
        canvas.drawRect(left, top, right, bottom, fillPaint);
    } else {
        // instead of creating the bitmap on the fly, we get the bitmap from singleton and draw directly.
        // image name can change in real time. So we grab the bitmap from the data.
        Bitmap bitmap = data.getBitmap(imageName);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, new RectF(left, top, right, bottom), null);
        }else{
            canvas.drawRect(left, top, right, bottom, fillPaint);
        }

//        int id = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
//            BitmapDrawable imageDrawable = (BitmapDrawable) context.getResources().getDrawable(id);
//            canvas.drawBitmap(imageDrawable.getBitmap(), null, new RectF(left, top, right, bottom), null);
    }
}

    //ignore the warning E/MediaPlayer: Should have subtitle controller already set
    // http://stackoverflow.com/questions/20087804/should-have-subtitle-controller-already-set-mediaplayer-error-android
    //--Sofia
    public void playSound(String soundName, Context context){
        Uri ur = data.getUri(soundName);
        if(ur != null){
            MediaPlayer mp = MediaPlayer.create(context, ur);
            mp.start();
        }
    }




    // Set the upper left corner coordinate (for dragging)
    public void dragLocation(float newX, float newY){
        bottom += newY - top;
        right += newX - left;
        left = newX;
        top = newY;
    }



    /*update query when the user click apply on the edit page activity. Shape is pointed by _id.
    */
    public String updateShapeQuery(){
        String geometry = left + " " + top + " " + right + " " + bottom;
        String query = "UPDATE shapes ";
        query += "SET name = '" + shapeName + "', geometry = '" + geometry +"', script = '" + script + "', ";
        query += " image = '" + imageName + "', text = '" + text + "', fontStyle = '" + fontStyle + "', ";
        query += "visible = ";
        query += visible ? 1 : 0;
        query += ", movable = ";
        query += movable ? 1 : 0;
        query += " WHERE _id = " + shapeID;
        System.out.println("updateQuery is:" + query);
        return query;
    }

}
