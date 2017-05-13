package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by sofiazhang on 3/4/17.
 * copy from Haoxuan Chen 3/4/17
 */

public class SingletonData {
    private static SQLiteDatabase db;
    private List<String> imageNameList;// list of images that a user can use to edit the game
    private List<String> soundNameList;// list of sound files that the user can use
    private Context gContext;
    private final String IMAGE_DIR_ASSETS = "myImages";// do not name folder as images!!! It will overlap with android
    private final String SOUND_DIR_ASSETS = "mySounds";// do not name folder as sounds!!!
    protected final String BACKGROUND_DIR_ASSETS = "myBackgrounds";// do not name folder as sounds!!!
    private  final String GAME_DIR_ASSETS = "myGame";

    private Map<String, Bitmap> imageResource; // a map of the image name mapping to the bitmap object
    private Map<String, Uri> soundResource; // a map of the sound name mapping to the Uri object Uri has to be converted to MediaPlayer
    private List<String> soundsFromAssets;// preexisting sound files in the app. Can never be deleted or modified by a user.
    public static SingletonData getInstance(Context context) {
        return new SingletonData(context);
    }
    //private List<String> pageList;// commented by Sofia

    // constructor
    private SingletonData(Context context) {
        gContext = context.getApplicationContext();
        if(db == null){
            boolean check = false;
            db = gContext.openOrCreateDatabase("editor", Context.MODE_PRIVATE, null);
            Cursor gamesCursor = db.rawQuery(
                    "SELECT * FROM sqlite_master WHERE type='table' AND name='games';", null);
            Cursor pagesCursor = db.rawQuery(
                    "SELECT * FROM sqlite_master WHERE type='table' AND name='pages';", null);
            Cursor shapesCursor = db.rawQuery(
                    "SELECT * FROM sqlite_master WHERE type='table' AND name='shapes';", null);
            Cursor imagesCursor = db.rawQuery(
                    "SELECT * FROM sqlite_master WHERE type='table' AND name='images';", null);
            Cursor soundsCursor = db.rawQuery(
                    "SELECT * FROM sqlite_master WHERE type='table' AND name='sounds';", null);
            if (gamesCursor.getCount() == 0){
                loadGameTable();
                check = true;
            }
            if (pagesCursor.getCount() == 0){
                loadPageTable();
            }
            if(shapesCursor.getCount() == 0){
                loadShapeTable();
            }
            if(imagesCursor.getCount() == 0) {
                loadImageTable();
            }
            if(soundsCursor.getCount() == 0) {
                loadSoundTable();
            }
            // get image and sound files from assets. These are used in the spinners.

            gamesCursor.close();
            pagesCursor.close();
            shapesCursor.close();
            soundsCursor.close();
            // todo: add default game
            if (check == true) loadGameFromResources();
        }
        //populate the image and sound resources
        imageResource = loadImagesFromResources();
        imageNameList = createImageNameListFromResources();
        // Sofia 3/17
        soundsFromAssets = loadFromAssets(SOUND_DIR_ASSETS);
        soundResource = loadSoundsFromResources();
        soundNameList = createSoundNameListFromResources();
    }
    /***********************************************************************************************
     *
     *  IMPLEMENTATIONS OF UPLOADING IMAGE AND SOUND FILES
     *
     * ***********************************************************************************************/
    // get all image files from the assets/images folder
    public List<String> loadFromAssets(String dir) {
        List<String> fileList = new ArrayList<>();
        AssetManager assetManager = gContext.getAssets();
        String[] files = new String[0];
        try {
            files = assetManager.list(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileList = Arrays.asList(files);
        for (int i = 0; i <fileList.size(); i++) {
            String base = fileList.get(i).substring(0, fileList.get(i).lastIndexOf('.'));// remove extension part
            fileList.set(i,base);
        }
        return fileList;
    }

    /* get all the user-uploaded images and the images from the assets/images folder.
    * generate bitmaps here for each image because it is expensive. So that onDraw can draw the bitmap quickly.
    * author: Sofia
    * */
    private Map<String, Bitmap> loadImagesFromResources() {
        Map<String, Bitmap> mp = new TreeMap<>();
        // upload bitmaps from the database
        Cursor cursor = db.rawQuery("SELECT * FROM images",null);
        if (cursor.moveToFirst()) {
            do {
                Bitmap bm = BitmapFactory.decodeFile(cursor.getString(1));
                mp.put(cursor.getString(0), bm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //upload bitmaps from the assets
        List<String> imagesFromAssets = loadFromAssets(IMAGE_DIR_ASSETS);
        for (String img : imagesFromAssets) {
            int id = gContext.getResources().getIdentifier(img, "drawable", gContext.getPackageName());
            BitmapDrawable imageDrawable = (BitmapDrawable) gContext.getResources().getDrawable(id);
            Bitmap bm = imageDrawable.getBitmap();
            mp.put(img, bm);
        }
        return mp;
    }

    private Map<String, Uri> loadSoundsFromResources() {
        Map<String, Uri> mp = new TreeMap<>();
        // upload bitmaps from the database
        Cursor cursor = db.rawQuery("SELECT * FROM sounds",null);
        if (cursor.moveToFirst()) {
            do {
                Uri ur = Uri.parse(cursor.getString(1));
                mp.put(cursor.getString(0), ur);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //upload uris from the assets

        for (String snd : soundsFromAssets) {
            String dataResourceDirectory = "raw";
            String dataResoruceFilename = snd;
            Uri ur=Uri.parse("android.resource://" + gContext.getPackageName() + "/" +
                    dataResourceDirectory + "/" + dataResoruceFilename);
            mp.put(snd, ur);
            // int id = gContext.getResources().getIdentifier(snd, "raw", gContext.getPackageName());
        }
        return mp;
    }

    private void loadGameFromResources() {
        AssetManager am = gContext.getAssets();
        try {
            InputStream is = am.open("basicGame.txt");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(isr);
            String line = "";
            while ((line = bf.readLine()) != null) {
                db.execSQL(line);
            }
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Return all the image names
    author: Sofia
    * */
    private List<String> createImageNameListFromResources() {
        return new ArrayList<String>(imageResource.keySet());
    }

    private List<String> createSoundNameListFromResources() {
        return new ArrayList<String>(soundResource.keySet());
    }

    // return the bitmap from the imageresource, return null if this map contains no mapping for the key
    // usage: drawShape calls this function to get the bitmap.
    //author: Sofia
    public Bitmap getBitmap(String imageName) {
        return imageResource.get(imageName);
    }
    // Return Uri from soundResource.
    //usage: playSound calls this function to get the Uri. The caller has to convert Uri to MediaPlayer to play the sound.
    //--Sofia
    public Uri getUri(String soundName) {
        return soundResource.get(soundName);
    }


    public List<String> getImageNameList() {
        return imageNameList;
    }
    public List<String> getSoundNameList() {
        return soundNameList;
    }
    //Sofia 3/17
    public List<String> getSoundsFromAssets() {
        return soundsFromAssets;
    }

    /* rename a sound name in the sound resources. Need to update the sound name in both resources map
     *  and the database
     *  Assume!!!!! the newSoundName does not overlap with any sound names in the database except for itself!!!!!!!
     *  author--Sofia
     *  */
    public void updateSoundName(String newSoundName, String origSoundName) {
        // update the resource map
        Uri uri = soundResource.remove(origSoundName);
        soundResource.put(newSoundName, uri);

        //update soundNamelist
        soundNameList.remove(origSoundName);
        soundNameList.add(newSoundName);

        //update database
        String execStr = "UPDATE sounds SET name = "+ "'" + newSoundName + "'" +" WHERE name = "+ "'" + origSoundName + "'"+";";
        System.out.println(execStr); // todo: remove! This is for debugging
        db.execSQL(execStr);
    }
    public void deleteSoundName(String soundName) {
        soundResource.remove(soundName);
        soundNameList.remove(soundName);
        String execStr = "DELETE FROM sounds WHERE name = " + "'" + soundName + "'" + ";";
        System.out.println(execStr); // todo: remove! This is for debugging
        db.execSQL(execStr);
    }

    /***********************************************************************************************
     *
     *  LOADING TABLES
     *
     * ***********************************************************************************************/



    // create a game table
    private void loadGameTable(){
        String setupStr = "CREATE TABLE games ("
                + "name TEXT, "
                + "lastPageIndex INTEGER, "
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        db.execSQL(setupStr);
    }

    /* create a page table
    * */
    private void loadPageTable(){
        String setupStr = "CREATE TABLE pages ("
                + "name TEXT,"
                + "gameIndex INTEGER, "
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        db.execSQL(setupStr);
    }
    /* create a shape table. Each shape has a unique shape ID and unique shape name.
    geometry has four floats:  left, top, right, bottom
    * */
    // added text and fontSize --Sofia
    private void loadShapeTable(){
        String setupStr = "CREATE TABLE shapes ("
                + "pageid INTEGER, name TEXT, geometry TEXT, script TEXT, image TEXT, "
                + "movable INTEGER, visible INTEGER, "
                + "text TEXT, fontStyle TEXT, "
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        db.execSQL(setupStr);
    }
    // Save the bitmap strings that the user uploaded, not the ones in res/assets.
    // So that next time the user restart the app the previously created shapes using uploaded
    // images are still viable and you can reuse these images.
    //author: Sofia
    private void loadImageTable(){
        String setupStr = "CREATE TABLE images ("
                + "name TEXT, bitmapstring TEXT, "
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        db.execSQL(setupStr);
    }

    // Save the uri strings that the user uploaded, not the ones in res/assets.
    // So that next time the user restart the app the previously created sounds using uploaded
    // audio files are still viable and you can reuse these audio files.
    //author: Sofia
    private void loadSoundTable(){
        String setupStr = "CREATE TABLE sounds ("
                + "name TEXT, uristring TEXT, "
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        db.execSQL(setupStr);
    }

    // pass a query string from anywhere and execute it directly on the database
    // this is used for debugging purposes by Sofia in uploadActivity.
    // --Sofia
    public Cursor getQueryCursor(String str){
        Cursor cursor = db.rawQuery(str, null);
        return cursor;
    }
    // Just execute the query. -- Haoxuan
    public void execQuery(String str){
        db.execSQL(str);
    }


    public void reset(){
        String resetGameStr = "DROP TABLE IF EXISTS games";
        String resetPageStr = "DROP TABLE IF EXISTS pages";
        String resetShapeStr = "DROP TABLE IF EXISTS shapes";
        String resetImageStr = "DROP TABLE IF EXISTS images";
        String resetSoundStr = "DROP TABLE IF EXISTS sounds";
        db.execSQL(resetGameStr);
        db.execSQL(resetPageStr);
        db.execSQL(resetShapeStr);
        db.execSQL(resetImageStr);
        db.execSQL(resetSoundStr);
        loadGameTable();
        loadPageTable();
        loadShapeTable();
        loadImageTable();
        loadSoundTable();
        loadGameFromResources();
        imageResource = loadImagesFromResources();// reset image name list.
        imageNameList = createImageNameListFromResources();
        soundResource = loadSoundsFromResources();// reset sound name list.
        soundNameList = createSoundNameListFromResources();
    }
    // get ALL the pages from database in a given game
    public List<String> getPageList(int gameIndex) {
        List<String> pageList = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT * FROM pages WHERE gameIndex = "+String.valueOf(gameIndex),null);
        if (cursor.moveToFirst()) {
            do {
                pageList.add(cursor.getString(0));// the 1st column is the page name
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pageList;
    }

    public List<Integer> getPageIDList(int gameIndex) {
        List<Integer> pageIDList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT _id FROM pages WHERE gameIndex = "+String.valueOf(gameIndex),null);
        if (cursor.moveToFirst()) {
            do {
                pageIDList.add(cursor.getInt(0));// the 1st column is the page name
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pageIDList;

    }

    // get all shapes from database in a given game
    // --Haolun
    public List<String> getShapeNameList(int gameIndex) {
        List<String> shapeList = new ArrayList<String>();
        List<Integer> pageIndexList = new ArrayList<Integer>();
        Cursor pageCursor = db.rawQuery("SELECT * from pages WHERE gameIndex = " + gameIndex,null);
        if (pageCursor.moveToFirst()) {
            do {
                Long pageIndex = pageCursor.getLong(pageCursor.getColumnIndex("_id"));
                pageIndexList.add(pageIndex.intValue());
            } while (pageCursor.moveToNext());
        }
        for (int pageIndex : pageIndexList) {
            Cursor cursor = db.rawQuery("SELECT * FROM shapes WHERE pageid = " + pageIndex,null);
            if (cursor.moveToFirst()) {
                do {
                    shapeList.add(cursor.getString(cursor.getColumnIndex("name")));//get the shape name column
                } while (cursor.moveToNext());
            }
        }
        //cursor.close();
        return shapeList;
    }
    // Update the script for shape name change. Also update the current shape name.
    public void updateScriptShape(String oldName, String newName){
        Cursor cursor = db.rawQuery("SELECT name,script FROM shapes", null);
        List<String> candidate = new LinkedList<>();
        if(cursor.moveToFirst()){
            do{
                String curName = cursor.getString(0);
                String curScript = cursor.getString(1);
                String[] clauses = curScript.split(";");
                StringBuilder sb = new StringBuilder();
                boolean match = false;
                for(String clause : clauses){
                    String[] words = clause.split("\\s+");
                    for(int i = 0; i < words.length; i++){
                        String curWord = words[i];
                        if(curWord.equals("drop") || curWord.equals("show") || curWord.equals("hide")){
                            if(words[i + 1].equals(oldName)){
                                words[i + 1] = newName;
                                match = true;
                            }
                        }
                        sb.append(curWord);
                        if(i < words.length - 1){
                            sb.append(" ");
                        }
                    }
                    sb.append(";");
                }
                if(match){
                    String newScript = sb.toString();
                    String query = "UPDATE shapes SET script = '" + newScript + "' WHERE name = '" + curName + "';";
                    candidate.add(query);
                }

            }while(cursor.moveToNext());
        }
        cursor.close();
        for(String query : candidate){
            db.execSQL(query);
        }
        String setNameQuery = "UPDATE shapes SET name = '" + newName + "' WHERE name = '" +  oldName + "';";
        db.execSQL(setNameQuery);

   }

    // Update the script for page name change.
    public void updateScriptPage(String oldName, String newName, int gameIndex) {
        Cursor pageCursor = db.rawQuery("SELECT _id FROM pages WHERE gameIndex = " + gameIndex,null);
        if (pageCursor.moveToFirst()) {
            List<Integer> pageIDsToUpdate = new ArrayList<>(); // select all the pageids from the current game
            do {
                Long longPageID = pageCursor.getLong(0);
                int pageID = longPageID.intValue();
                pageIDsToUpdate.add(pageID);
            } while (pageCursor.moveToNext());
            for (int pageID : pageIDsToUpdate) {
                Cursor shapeCursor = db.rawQuery("SELECT name,script FROM shapes WHERE pageid = " + pageID,null);
                if (shapeCursor.moveToFirst()) {
                    do {
                        String curName = shapeCursor.getString(0);
                        String curScript = shapeCursor.getString(1);
                        if (curScript.contains("goto") && curScript.contains(oldName)) {
                            int beginning, end;
                            beginning = curScript.indexOf("goto") + 4;
                            end = beginning + oldName.length() + 1;
                            String left = curScript.substring(0, beginning + 1);
                            String right = curScript.substring(end);
                            String newScript = left + newName + right;
                            System.out.println(newScript);
                            String query = "UPDATE shapes SET script = '" + newScript + "' WHERE name = '" + curName + "';";
                            db.execSQL(query);
                        }
                    } while (shapeCursor.moveToNext());
                }
                shapeCursor.close();
            }
        }
        pageCursor.close();
    }

    // get ALL the shapes from database regardless what page it is from (For shape name setup)
    public List<String> getShapeNameList() {
        List<String> shapeList = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT * FROM shapes",null);
        if (cursor.moveToFirst()) {
            do {
                shapeList.add(cursor.getString(cursor.getColumnIndex("name")));//get the shape name column
            } while (cursor.moveToNext());
        }
        cursor.close();
        return shapeList;
    }



    public int getPageID(String name, int gameIndex) {
        Cursor cursor = db.rawQuery("SELECT * FROM pages WHERE name = '"+ name + "' AND gameIndex = " + String.valueOf(gameIndex),null);
        if (cursor.moveToFirst()) {
            Long temp = cursor.getLong(cursor.getColumnIndex("_id"));
            return temp.intValue();
        }
        cursor.close();
        return -1;//if empty set, return sentinel value
    }


    // changed by Sofia
    // create a new page and return page id as integer
    public int addNewPage(int lastPageIndex, int gameIndex) {

        db.execSQL("UPDATE games SET lastPageIndex = "+String.valueOf(lastPageIndex+1)+" WHERE _id = "+String.valueOf(gameIndex)+";");
        db.execSQL("INSERT INTO pages VALUES ('page"+String.valueOf(lastPageIndex+1)+"',"+String.valueOf(gameIndex)+",NULL);");
        Cursor cursor = db.rawQuery("SELECT * FROM pages",null);
        cursor.moveToLast();
        Long pageIndex = cursor.getLong(cursor.getColumnIndex("_id"));
        cursor.close();
        return pageIndex.intValue();

    }
    // check duplication -- Changed by Sofia 3/14
    public boolean addNewGame(String gameNameStr) {
        List<String> gameNameList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT name FROM games", null);
        if (cursor.moveToFirst()) {
            do {
                gameNameList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        if (gameNameList.contains(gameNameStr)) return false;

        db.execSQL("INSERT INTO games VALUES ('"+gameNameStr+"',0,NULL);");
        return true;
    }
    // add a uploaded image to the database. called by uploadActivity when a user add an image from gallery
    // the image is passed in as a bitmap decodable string and saved in database directly and save to the imageResource
    // as Bitmaps.
    // author: sofia
    public boolean addNewImage(String bitmapStr) {
        List<String> bitmapList = new ArrayList<>();
        // check if this file has already been uploaded
        Cursor cursor = db.rawQuery("SELECT bitmapstring FROM images", null);
        if (cursor.moveToFirst()) {
            do {
                bitmapList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        if (bitmapList.contains(bitmapStr)) return false; // this file has already been uploaded
        cursor = db.rawQuery("SELECT _id FROM images", null);
        int lastID = 0;
        if (cursor.moveToLast()){
            lastID= cursor.getInt(0);
        }
        String defaultImageName = "gallery_image" + Integer.toString(lastID+1);
        String execStr = "INSERT INTO images VALUES ("
                + "'" + defaultImageName + "'" + ", "
                + "'" + bitmapStr + "'" + ", "
                + "NULL);";

        System.out.println(execStr);// debug todo: delete this!!!!!!!!!!!!
        db.execSQL(execStr);
        cursor.close();
        Bitmap bm = BitmapFactory.decodeFile(bitmapStr);
        imageResource.put(defaultImageName, bm);
        imageNameList.add(defaultImageName);
        return true;
    }


    public boolean addNewSound(String uriStr) {
        List<String> uriList = new ArrayList<>();
        // check if this file has already been uploaded
        Cursor cursor = db.rawQuery("SELECT uristring FROM sounds", null);
        if (cursor.moveToFirst()) {
            do {
                uriList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        if (uriList.contains(uriStr)) return false; // this file has already been uploaded
        cursor = db.rawQuery("SELECT _id FROM sounds", null);
        int lastID = 0;
        if (cursor.moveToLast()){
            lastID= cursor.getInt(0);
        }
        String defaultSoundName = "music_audio" + Integer.toString(lastID+1);
        String execStr = "INSERT INTO sounds VALUES ("
                + "'" + defaultSoundName + "'" + ", "
                + "'" + uriStr + "'" + ", "
                + "NULL);";


        db.execSQL(execStr);
        cursor.close();
        Uri ur = Uri.parse(uriStr);
        soundResource.put(defaultSoundName, ur);
        soundNameList.add(defaultSoundName);
        return true;
    }

    public int getGameIndex(String gameName) {
        Cursor cursor = db.rawQuery("SELECT * FROM games WHERE name = '"+gameName+"'",null);
        int idx = -1;
        if (cursor.moveToFirst()) {
            Long gameIndex = cursor.getLong(cursor.getColumnIndex("_id"));
            idx = gameIndex.intValue();
        }
        cursor.close();
        return idx;
    }

    public List<String> getGameList() {
        List<String> gameList = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT * FROM games",null);
        if (cursor.moveToFirst()) {
            do {
                gameList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return gameList;
    }

    public int getLastPageIndex(String gameName) {
        Cursor cursor = db.rawQuery("SELECT * FROM games WHERE name = '"+gameName+"'",null);
        int idx = 0;
        if (cursor.moveToFirst()) {
            idx = cursor.getInt(cursor.getColumnIndex("lastPageIndex"));
        }
        cursor.close();
        return idx;
    }

    // return a list of shape IDs given the page ID.
    // One pageID can have multiple shapes but each shape ID is unique. You can populate the Shape objects from here.
    // author: Sofia
    public List<Integer> getShapesFromPage(int pageID) {
        List<Integer> shapeIDs = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT _id FROM shapes WHERE pageid = " + pageID + ";", null);
        if (cursor.moveToFirst()) {
            do {
                shapeIDs.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return shapeIDs;
    }

    /*get a list of page name.
    * Usage: called when user tries to change the page name and click the apply button on editPageActivity.
    * If the new pagename already exist, then page will not be renamed.
    * --Sofia 3/14 */
    public List<String> getPageNameList(int gameID) {
        List<String> pageNameList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT name FROM pages WHERE gameIndex = " + gameID, null);
        if (cursor.moveToFirst()) {
            do {
                pageNameList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        return pageNameList;
    }
    /*update the pageName in the database given the page ID.
    * Called then the apply button is clicked in the editPage Activity and no shape needs to be highlighted.
     * Assume that pageID is not 1! page1 cannot be renamed!
     * --Sofia 3/14 */
    public void setPageName(String pageNameStr, int pageID) {
        Cursor cursor = db.rawQuery("SELECT * FROM pages WHERE _id = " + String.valueOf(pageID),null);
        if (cursor.moveToFirst()){
            String query = "UPDATE pages ";
            query += "SET name = " + "'" + pageNameStr + "'";
            query += " WHERE _id = " + pageID + ";";
            db.execSQL(query);
        }
        cursor.close();
    }

    /***********************************************************************************************
     *
     create/remove/update a shape from the shapes table in the database given a shape.
     all shape properties should be retrieved or updated by shapeID using the database
     Usage: These functions are called when an onTouch Event is handled in custom view.
     Assume: shape ID passed in is valid.
     *
     ************************************************************************************************/
      /*Permanently remove the shape from the database. */
    public void deleteShapeOnErase(int shapeID) {
        String execStr = "DELETE FROM shapes WHERE _id = " + shapeID + ";";
        db.execSQL(execStr);
    }

    /*update the shape properties in the database.
    * Called when the apply button is clicked in the editPageActivity and this shape is highlighted. */
    public void updateShapeOnSelect(Shape shape) {
        String execStr = shape.updateShapeQuery();
        db.execSQL(execStr);
    }

    /*When the user make a new shape on canvas, a new shape is immediately created leaving all other fields empty
    Return the shape ID to the client so the client has a reference to this shape.
    the geometry string has to be the following: left, top, right, bottom. Client is responsible to provide a valid string
      String setupStr = "CREATE TABLE shapes ("
                + "pageid INTEGER, name TEXT, geometry TEXT, script TEXT, image TEXT, "
                + "movable INTEGER, visible INTEGER, "
                + "text TEXT, fontSize INTEGER, "
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
    * */
    public int createNewShapeOnDrag(int pageID, String imageName, String geometry) {
        //String defaultShapeName = "shape" + Integer.toString(getShapeCount()+1);//todo: cannot getCount
        Cursor cursor = db.rawQuery("SELECT _id FROM shapes", null);
        int lastID = 0;
        if (cursor.moveToLast()){
            lastID= cursor.getInt(0);
        }
        String defaultShapeName = "shape" + Integer.toString(lastID+1);//todo: should be able to distinguish each game
        String defaultScript = "";
        String defaultText = "";
        String defaultFontString = Shape.DEFAULT_FONTSTRING;
        int defaultMovable = 1;
        int defaultVisible = 1;
        String execStr = "INSERT INTO shapes VALUES (" + pageID + ","
                + "'" + defaultShapeName + "'" + ", "
                + "'" + geometry + "'" + ", "
                + "'" + defaultScript + "'" + ", "
                + "'" + imageName + "'" + ", "
                +  defaultMovable + ", "
                +  defaultVisible + ", "
                + "'" + defaultText + "'" + ", "
                +  "'" + defaultFontString + "'" + ", "
                + "NULL);";

        //System.out.println(execStr);
        db.execSQL(execStr);
        // we don't know where it's added. So have to make another query to get the shape ID
        cursor = db.rawQuery("SELECT _id FROM shapes WHERE name = " + "'" + defaultShapeName + "'", null);// get the shape id
        int idVal = -1;
        if (cursor.moveToFirst()) {
            Long id = cursor.getLong(cursor.getColumnIndex("_id"));
            idVal = id.intValue();
        }
        cursor.close();
        return idVal; // return an exception
    }




    /***********************************************************************************************
     get shapeName, imageName, geometry, text, fontsize, script all from the database given shapeID.
     all shape properties should be retrieved or updated by shapeID using the database, taking advantage of the
     fact that shape ID is unique.
     Usage: These functions are called in custom view to populate the list of Shape objects
     Assume: shape ID passed in is valid.
     ************************************************************************************************/
    public String getPageName(int pageID) {
        Cursor cursor = db.rawQuery("SELECT name FROM pages WHERE _id = " + pageID + ";", null);
        String output = "";
        if (cursor.moveToFirst()) {
            output += cursor.getString(0);
        }
        cursor.close();
        return output;
    }
    public String getShapeName(int shapeID) {
        Cursor cursor = db.rawQuery("SELECT name FROM shapes WHERE _id = " + shapeID + ";", null);
        String output = "";
        if (cursor.moveToFirst()) {
            output += cursor.getString(0);
        }
        cursor.close();
        return output;
    }

    public String getImageName(int shapeID) {
        Cursor cursor = db.rawQuery("SELECT image FROM shapes WHERE _id = " + shapeID+ ";", null);
        String output = "";
        if (cursor.moveToFirst()) {
            output += cursor.getString(0);
        }
        cursor.close();
        return output;
    }

    public String getText(int shapeID) {
        Cursor cursor = db.rawQuery("SELECT text FROM shapes WHERE _id = " + shapeID+ ";", null);
        String output = "";
        if (cursor.moveToFirst()) {
            output += cursor.getString(0);
        }
        cursor.close();
        return output;
    }
    public String getTextFont(int shapeID) {
        Cursor cursor = db.rawQuery("SELECT fontStyle FROM shapes WHERE _id = " + shapeID+ ";", null);
        String output = "";
        if (cursor.moveToFirst()) {
            output += cursor.getString(0);
        }
        cursor.close();
        return output;
    }

    public String getScript(int shapeID) {
        Cursor cursor = db.rawQuery("SELECT script FROM shapes WHERE _id = " + shapeID+ ";", null);
        String output = "";
        if (cursor.moveToFirst()) {
            output += cursor.getString(0);
        }
        cursor.close();
        return output;
    }

    public String getShapeGeometry(int shapeID) {
        Cursor cursor = db.rawQuery("SELECT geometry FROM shapes WHERE _id = " + shapeID+ ";", null);
        String output = "";
        if (cursor.moveToFirst()) {
            output += cursor.getString(0);
        }
        cursor.close();
        return output;
    }

    public int getMovable(int shapeID) {
        Cursor cursor = db.rawQuery("SELECT movable FROM shapes WHERE _id = " + shapeID+ ";", null);
        int move = -1;
        if (cursor.moveToFirst()) {
            move = cursor.getInt(0);
        }
        cursor.close();
        return move;
    }

    public int getVisible(int shapeID) {
        Cursor cursor = db.rawQuery("SELECT visible FROM shapes WHERE _id = " + shapeID+ ";", null);
        int vis = -1;
        if (cursor.moveToFirst()) {
            vis = cursor.getInt(0);
        }
        cursor.close();
        return vis;
    }

    //overload of the previous getPageID(String name)
    public int getPageID(int shapeID) {

        Cursor cursor = db.rawQuery("SELECT pageid FROM shapes WHERE _id = " + shapeID, null);
        int pageId = -1;
        if (cursor.moveToFirst()) {
            Long id = cursor.getLong(cursor.getColumnIndex("pageid"));
            //System.out.println(id.intValue());
            pageId = id.intValue();
        }
        cursor.close();
        return pageId;
    }


    //overload of the previous getLastPageIndex(String gameName)
    public int getLastPageIndex(int gameIndex) {
        Cursor cursor = db.rawQuery("SELECT lastPageIndex FROM games WHERE _id = " + String.valueOf(gameIndex),null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }
        return -1;
    }

    public void deletePage(int pageID) {
        String pageName = "";
        Cursor cursor = db.rawQuery("SELECT * FROM pages WHERE _id = " + pageID,null);
        if (cursor.moveToFirst()) pageName = cursor.getString(cursor.getColumnIndex("name"));
        cursor.close();
        if (!pageName.equals("page1")) {
            String deletePageStr = "DELETE FROM pages WHERE _id = " + pageID + ";";
            db.execSQL(deletePageStr);
            String deleteShapeStr = "DELETE FROM shapes WHERE pageid = " + pageID + ";";
            db.execSQL(deleteShapeStr);
        }
    }

    public void deleteImage(String imageName) {
        String deleteStr = "DELETE FROM images WHERE name = '" + imageName + "';"; // delete image from image table
        db.execSQL(deleteStr);
        String deleteImage = "DELETE FROM shapes WHERE image = '" + imageName + "';"; // delete image from shape table
        db.execSQL(deleteImage);
        imageNameList.remove(imageName); // delete image from imagelist
        imageResource.remove(imageName); // delete image from bitmap
    }

    public void renameImage(String oldName, String newName) {
        String renameStr = "UPDATE images SET name = '" + newName + "' WHERE name = '" + oldName + "';"; // rename image in image table
        db.execSQL(renameStr);
        String renameImage = "UPDATE shapes SET image = '" + newName + "' WHERE image = '" + oldName + "';"; // rename image in shape table
        db.execSQL(renameImage);
        Bitmap bm = imageResource.get(oldName);
        imageResource.remove(oldName);
        imageResource.put(newName, bm); // rename image in the bitmap
        imageNameList.set(imageNameList.indexOf(oldName), newName);
    }
}
