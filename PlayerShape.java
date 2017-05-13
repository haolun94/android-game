package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by haoxuanchen on 3/10/17.
 */

public class PlayerShape extends Shape{
    private float origH, origW;
    private boolean clickable;
    private String script;
    private Map <String, String> triggerTable;
    private static final int PLAY = 1;
    private static final int HIDE = 2;
    private static final int SHOW = 3;
    private static final int GOTO = 4;
    private static Map<String, PlayerShape> shapeTable = new HashMap<>();


    public PlayerShape(int shapeID, SingletonData data){
        super(shapeID, data);

        clickable = true;

        script = super.getScript();

        origW = super.getRight() - super.getLeft();
        origH = super.getBottom() - super.getTop();

        triggerTable = new HashMap<>();

        if(!script.equalsIgnoreCase(""))parseScript(script);

        shapeTable.put(super.getShapeName(), this);



    }


    // Parse the script. Split it to several clauses and parse the trigger and the corresponding
    // actions. Ignore the same trigger between clauses.
    private void parseScript(String script){
        String[] clauses = script.split(";");
        for(String clause : clauses){
            String[] words = clause.split("\\s+");
            String trigger = words[0] + " " + words[1];
            String action = "";
            int startidx = 2;
            if(words[1].equals("drop")){
                trigger += " " + words[2];
                startidx += 1;
            }
            for(int i = startidx; i < words.length; i++){
                action += words[i] + " ";
            }
            if(!triggerTable.containsKey(trigger)){
                triggerTable.put(trigger, action);
            }
        }

    }

    @Override
    public void drawShape(Canvas canvas, Context context){
        if(this.getVisible()){
            super.drawShape(canvas, context);
        }
    }

    public boolean isClickable(){
        return clickable;
    }

    public boolean tableContainsKey(String trigger){
        return triggerTable.containsKey(trigger);
    }

    //Execute the action triggered by the trigger word. Called in the custom view.
    public int exec(String trigger, Context context, View view){
        int res = -1;
        if(triggerTable.containsKey(trigger)){
            String action = triggerTable.get(trigger);
            String[] actionList = action.split("\\s+");
            for(int i = 0; i < actionList.length - 1; i+=2){
                String verb = actionList[i];
                String object = actionList[i + 1];
                if(verb.equalsIgnoreCase("play")){
                    super.playSound(object, context);
                }else if(verb.equalsIgnoreCase("hide")){
                    hide(object);
                }else if(verb.equalsIgnoreCase("show")){
                    show(object);
                }else if(verb.equalsIgnoreCase("goto")){
                    res = goTo(object, context, view);
                }
            }
        }
        return res;
    }
    // TODO: Shirnk the shape to fit the possession area.
    public void shrink(){
        if(super.getBottom() > PlayerCustomView.TOTAL_H){
            float newH = PlayerCustomView.TOTAL_H - super.getTop();
            float newW = newH / origH * origW;
            float newLeft = super.getLeft();
            float newRight = newLeft + newW;
            float newTop = super.getTop();
            float newBottom = newTop + newH;
            super.setGeometry(newLeft, newTop, newRight, newBottom);
        }
    }
    public void restoreSize(){
        float newBottom = super.getBottom();
        float newTop = newBottom - origH;
        float newRight = super.getRight();
        float newLeft = newRight - origW;
        super.setGeometry(newLeft, newTop, newRight, newBottom);
    }



    public void setClickable(boolean click){
        clickable = click;
    }

    public static PlayerShape getShape(String name){
        return shapeTable.get(name);
    }

    public void hide(String name){
        PlayerShape shape = PlayerShape.getShape(name);
        if(shape != null){
            shape.setVisible(false);
            shape.setClickable(false);
        }

    }

    public void show(String name){
        PlayerShape shape = PlayerShape.getShape(name);
        if(shape != null){
            shape.setVisible(true);
            shape.setClickable(true);
        }

    }


    public int goTo(String page, Context context, View view){
        Intent intent = ((Activity)view.getContext()).getIntent();
        int gameID = intent.getIntExtra("gameIndex", 1);
        SingletonData data = SingletonData.getInstance(context);
        return data.getPageID(page, gameID);
    }



}
