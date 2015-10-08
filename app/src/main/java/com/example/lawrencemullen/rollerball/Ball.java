package com.example.lawrencemullen.rollerball;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by lawrencemullen on 10/4/15.
 */
public class Ball {
    protected float x,y;
    private float width, height;
    private Bitmap bitmap;
    private int screenWidth, screenHeight;
    private final float SCALE = .33333f;

    public Ball(Context context) {

        // get the image
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ball);

        // scale the size
        width = bitmap.getWidth() * SCALE;
        height = bitmap.getHeight() * SCALE;

        // figure out the screen width
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;


    }

    public void doDraw(Canvas canvas) {
        // draw the ball
        if((x-width/2)<0){
            x = width/2;
        }
        if((x+width/2)>screenWidth){
            x = screenWidth-width/2;
        }
        if((y-height/2)<0){
            y = height/2;
        }
        if((y+height/2)>screenHeight){
            y = screenHeight-height/2;
        }

        canvas.drawBitmap(bitmap,
                null,
                new Rect((int) (x - width/2), (int) (y- height/2),
                        (int) (x + width/2), (int) (y + height/2)),
                null);

    }

    public void doUpdate(double elapsed, float xValue ,float yValue) {
        // easing based on touch point
        x = ( x + xValue*2 );
        y =  ( y +yValue*2 );
    }

    public float getWidth() {
        return width;
    }
    public float getHeight() {
        return height;
    }

}
