package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;

/**
 * Created by haoxuanchen on 3/14/17.
 * Referrence : https://github.com/SueSmith/android-drawing-app/blob/master/src/com/example/drawingfun/DrawingView.java
 * Referrence : https://code.tutsplus.com/tutorials/android-sdk-create-a-drawing-app-touch-interaction--mobile-19202
 */

public class DrawingCustomView extends View {
    private Path path; //drawing path
    private Paint drawPaint, canvasPaint, bgPaint; //drawing and canvas paint
    private int paintColor = 0xFF660000, paintAlpha = 255; //initial color
    private Canvas drawCanvas; //canvas
    private Bitmap canvasBitmap; //canvas bitmap
    private float brushSize; //brush sizes
    private boolean erase=false;
    private static float SMALL = 10;
    private static float MEDIUM = 20;
    private static float LARGE = 30;
    public DrawingCustomView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init(){
        path = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0.0f, 0.0f, getWidth(), getHeight(), bgPaint);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(path, drawPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    // Respond to the draw action
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        RadioGroup group = (RadioGroup)((Activity)getContext()).findViewById(R.id.mode);
        int mode = group.getCheckedRadioButtonId();
        if(mode == R.id.draw){
            setErase(false);
        }else{
            setErase(true);
        }
        setBrushSize();
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(touchX, touchY);
                if(erase){
                    drawCanvas.drawPath(path, drawPaint);
                    path.reset();
                    path.moveTo(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!erase){
                    path.lineTo(touchX, touchY);
                    drawCanvas.drawPath(path, drawPaint);
                    path.reset();
                }
                path.reset();
        }

        invalidate();
        return true;

    }

    public void setColor(String newColor){
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
        invalidate();
    }

    public void setErase(boolean isErase){
        erase = isErase;
        if(erase){
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            //drawPaint.setXfermode(clear);
        }else{
            drawPaint.setXfermode(null);
        }

    }

    public void setBrushSize(){
        RadioGroup group = (RadioGroup)((Activity)getContext()).findViewById(R.id.brush);
        int brushId = group.getCheckedRadioButtonId();
        switch (brushId){
            case R.id.small:
                brushSize = SMALL ;
                break;
            case R.id.medium:
                brushSize = MEDIUM;
                break;
            case R.id.large:
                brushSize = LARGE;

        }
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setPaintAlpha(int newAlpha){
        paintAlpha = Math.round((float) newAlpha / 100 * 255);
        drawPaint.setColor(paintColor);
        drawPaint.setAlpha(paintAlpha);
    }

    public void reset(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
