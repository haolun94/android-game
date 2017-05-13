package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;
import android.widget.Spinner;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xianbingcheng on 3/8/17.
 */


public class PlayerCustomView extends View {
    protected Context context;
    protected SingletonData data;
    protected List<PlayerShape> shapeList = new ArrayList<>();
    protected int gameID;
    protected float x1,y1,x2,y2; //ACTION_DOWN and ACTION_UP
    protected float xx, yy;      //ACTION_MOVE
    protected boolean pageChanged;
    protected PlayerShape aboveShape;
    protected PlayerShape underShape;
    protected float preLeft, preTop, preRight, preBottom;
    protected static float HEIGHT = 450;
    protected static float TOTAL_H = 600;
    protected Paint myPaint;
    protected float alpha = 0;
    protected static final int FADE_SEC = 300;
    private static final int FADE_STEP = 30;          // 30ms refresh
    // Calculate our alpha step from our fade parameters
    private static final double ALPHA_STEP = 1.0 / (FADE_SEC / FADE_STEP);
    public PlayerCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        data = SingletonData.getInstance(context);
        Intent intent = ((Activity) getContext()).getIntent();
        gameID = intent.getIntExtra("gameIndex", 1);
        int pageOneID = data.getPageID("page1", gameID);
        preLoadPages();
        shapeList = getShapeListFromPage(pageOneID);
        pageChanged = true;
        init();
    }
    public void preLoadPages(){
        List<Integer> pageIDlist = data.getPageIDList(gameID);
        for(int id : pageIDlist){
            getShapeListFromPage(id);
        }

    }
    public void setBackground() {
        Spinner bgSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.background_name_spinner);
        String selectedBackground = bgSpinner.getSelectedItem().toString();
        if(!selectedBackground.equals(PlayGameActivity.DEFAULTBG)){
            Resources resources = context.getResources();
            final int resourceId = resources.getIdentifier(selectedBackground, "drawable",
                    context.getPackageName());
            setBackgroundResource(resourceId);
        }else{
            setBackgroundResource(0);
        }


    }

    private void init(){
        myPaint = new Paint();
        myPaint.setColor(Color.BLACK);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(5.0f);
    }
    // Get the shape list of a certain page from the page id.
    private List<PlayerShape> getShapeListFromPage(int pageID) {

        Page p;
        if(!Page.containsPage(pageID)){
            p = new Page(pageID, data);
        }else{
            p = Page.getPage(pageID);
        }
        return p.shapeList;

    }

    //automatically trigger "on enter" of all shapes and set the page changed flag to false;
    public void onEnter(){
        for(PlayerShape shape : shapeList){
            String trigger ="on enter";
            if(shape.tableContainsKey(trigger)){
                int nexPageID = shape.exec(trigger, context, this);
                if(nexPageID>=0){
                    shapeList = getShapeListFromPage(nexPageID);
                    pageChanged = true;

                }else{
                    pageChanged = false;
                }
                invalidate();
                return;
            }
        }
        pageChanged = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //pageCustomView
        canvas.drawRect(0.0f,0.0f,getWidth(),getHeight(),myPaint);
        //possessionArea
        canvas.drawRect(0.0f,3*getHeight()/4,getWidth(),getHeight(),myPaint);

        for (PlayerShape s : shapeList) {
            s.drawShape(canvas, context);
        }
        for(PlayerShape s : Page.getPossesion()){
            s.drawShape(canvas, context);
        }
        if(pageChanged){
            if(alpha <= 1){
                setAlpha(alpha);
                alpha += ALPHA_STEP;
                postInvalidateDelayed(FADE_STEP);
            }else{
                alpha = 0;
                onEnter();
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            // Action down event: First find the shape selected (aboveShape) and
            // execute the "on click" if it contains this trigger (Except in the possession area).
            // Then store the previous loaction of the above shape for the later return.
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                if(y1 < HEIGHT){
                    for(PlayerShape shape : shapeList){
                        if(shape.isClickable() && shape.containsPoint(x1,y1)){
                            aboveShape = shape;
                            int pageID = aboveShape.exec("on click", context, this);
                            if(pageID >= 0) {
                                pageChanged = true;
                                shapeList = getShapeListFromPage(pageID);
                            }

                            break;
                        }
                    }
                }else{
                    for(PlayerShape shape: Page.getPossesion()){
                        if(shape.containsPoint(x1, y1)){
                            aboveShape = shape;
                            break;
                        }
                    }
                }
                if(aboveShape != null){
                    preLeft = aboveShape.getLeft();
                    preTop = aboveShape.getTop();
                    preRight = aboveShape.getRight();
                    preBottom = aboveShape.getBottom();
                }

                invalidate();
                break;

            // Action up Event: First check if it is dragged from page to possession or the other
            // around. Then check the under shape's on drop action. If the undershape dosen't have
            // on drop trigger, the above shape return to its original position
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();

                if(aboveShape != null){
                    float curLeft = aboveShape.getLeft();
                    float curTop = aboveShape.getTop();
                    float curRight = aboveShape.getRight();
                    float curBottom = aboveShape.getBottom();
                    // Deal with the boundary case
                    if(curBottom > HEIGHT && curTop < HEIGHT){
                        if(preTop < HEIGHT ){
                            if(Math.abs(curTop - HEIGHT) < Math.abs(curBottom - HEIGHT)){
                                aboveShape.setGeometry(curLeft, HEIGHT, curRight, curBottom - curTop + HEIGHT);
                                shapeList.remove(aboveShape);
                                Page.pageToPossession(aboveShape);
                            }else{
                                aboveShape.setGeometry(curLeft, curTop - curBottom + HEIGHT, curRight, HEIGHT);
                            }
                        }else{
                            if(Math.abs(curTop - HEIGHT) > Math.abs(curBottom - HEIGHT)){
                                aboveShape.setGeometry(curLeft, curTop - curBottom + HEIGHT, curRight, HEIGHT);
                                shapeList.add(aboveShape);
                                Page.possessionToPage(aboveShape);
                            }else{
                                aboveShape.setGeometry(curLeft, HEIGHT, curRight, curBottom - curTop + HEIGHT);

                            }
                        }
                    // Common dragging cases
                    }else{
                        if(preTop < HEIGHT && curTop >= HEIGHT){
                            shapeList.remove(aboveShape);
                            Page.pageToPossession(aboveShape);
                        }
                        if(preTop >= HEIGHT && curTop < HEIGHT){
                            shapeList.add(aboveShape);
                            Page.possessionToPage(aboveShape);
                        }
                        if(preTop >= HEIGHT && curTop >= HEIGHT){
                            aboveShape.shrink();
                        }
                    }
                }

                if(underShape != null && underShape.containsPoint(x2, y2) && underShape != aboveShape){
                    String trigger = "on drop " + aboveShape.getShapeName();
                    if(underShape.tableContainsKey(trigger)){
                        underShape.unHighlight();
                        int pageID = underShape.exec(trigger, context, this);
                        if(pageID >= 0){
                            pageChanged = true;
                            shapeList = getShapeListFromPage(pageID);
                        }
                    }else{
                        aboveShape.setGeometry(preLeft, preTop, preRight, preBottom);
                        if(y1 > HEIGHT && y2 < HEIGHT){
                            shapeList.remove(aboveShape);
                            Page.pageToPossession(aboveShape);
                        }
                    }
                }
                aboveShape = null;
                underShape = null;
                invalidate();
                break;

            // Action move event: First one is implementing the dragging, which constantly set the
            // geometry of the aboveshape. The second part is dealing with the under shape which
            // has the on drop trigger, highlight it when the above shape is above it and unhighlight
            // it when the aboveshape is away.
            case MotionEvent.ACTION_MOVE:
                if(aboveShape != null && aboveShape.getMovable()){
                    xx = event.getX();
                    yy = event.getY();

                    //drag
                    float xMove = xx-x1;
                    float yMove = yy-y1;
                    if(aboveShape != null){
                        float newLeft = preLeft + xMove;
                        float newTop = preTop + yMove;
                        float newRight = preRight + xMove;
                        float newBottom = preBottom + yMove;

                        aboveShape.setGeometry(newLeft, newTop, newRight, newBottom);

                    }
                    if(underShape != null && !underShape.containsPoint(xx, yy)){
                        underShape.unHighlight();
                    }

                    //detect shape beneath
                    for(PlayerShape shape : shapeList){
                        if(shape.containsPoint(xx,yy) && shape != aboveShape){
                            String trigger = "on drop " + aboveShape.getShapeName();
                            underShape = shape;
                            if(underShape.tableContainsKey(trigger)){
                                underShape.highlight();
                            }
                            break;

                        }
                    }



                }
                invalidate();


        }
        return true;
    }

}
