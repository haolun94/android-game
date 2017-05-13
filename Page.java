package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sofiazhang on 3/3/17.
 */
// a simple page object for testing Shapes
public class Page {
    protected List<PlayerShape> shapeList;
    private static List<PlayerShape> possesion = new LinkedList<>();
    private final int pageID;// pageID cannot be changed after initialized
    private static Map<Integer, Page> pageTable = new HashMap<>();

    // given the page ID, retrive all the shapes in this page and populate the shape list
    public Page(int pageID, SingletonData data) {
        this.pageID = pageID;
        shapeList = new LinkedList<>();
        List<Integer> shapeIDs = data.getShapesFromPage(pageID);
        for (int id : shapeIDs) {
            PlayerShape shape = new PlayerShape(id, data);
            shapeList.add(shape);
        }
        pageTable.put(pageID, this);
    }

    // getters and setters
    public int getpageID() {
        return pageID;
    }

    // move an object from page to possession
    public static void pageToPossession(PlayerShape shape) {
        shape.shrink();
        possesion.add(shape);

    }

    // move an object from possession to page
    public static void possessionToPage(PlayerShape shape) {
        shape.restoreSize();
        possesion.remove(shape);

    }

    public static void reset(){
        pageTable.clear();
        possesion.clear();
    }

    public static boolean containsPage(int id){
        return pageTable.containsKey(id);
    }

    public static Page getPage(int id){
        return pageTable.get(id);
    }

    public static List<PlayerShape> getPossesion(){
        return possesion;
    }

}
