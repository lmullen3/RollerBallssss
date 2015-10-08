package com.example.lawrencemullen.rollerball;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


/**
 * Created by lawrencemullen and maddie chilli on 10/4/15.
 */
public class BoardView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {


    private GameLoopThread thread;
    private SurfaceHolder surfaceHolder;
    private Context context;
    private Bitmap bitmap;
    private SensorManager sensorManager;
    private Sensor mAccel;
    private float x, y;
    float screenWidth;
    float screenHeight;



    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // remember the context for finding resources
        this.context = context;

        // want to know when the surface changes
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // game loop thread
        thread = new GameLoopThread();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

    }


    // SurfaceHolder.Callback methods:
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // thread exists, but is in terminated state
        sensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);

        if (thread.getState() == Thread.State.TERMINATED) {
            thread = new GameLoopThread();
        }

        // start the game loop
        thread.setIsRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        thread.setIsRunning(false);
        sensorManager.unregisterListener(this);

        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x = event.values[0];
        y = event.values[1];

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    // Game Loop Thread
    private class GameLoopThread extends Thread {
        private boolean isRunning = false;
        private long lastTime;

        // the ball
        private Ball ball;
        private Spot spot;



        public GameLoopThread() {
            spot = new Spot(context);
            ball = new Ball(context);
        }
        double insideTime = 0;

        private void checkBall(double elapsed) {

            if (((ball.x + ball.getWidth() / 2) < (spot.x + spot.getWidth() / 2)) && ((ball.x - ball.getWidth() / 2) > (spot.x - spot.getWidth() / 2)) &&
                    ((ball.y + ball.getHeight() / 2) < (spot.y + spot.getHeight() / 2)) &&
                    ((ball.y - ball.getHeight() / 2) > (spot.y - spot.getHeight() / 2))) {

                insideTime = insideTime + elapsed;
                System.out.println(insideTime);
                if (insideTime >= 3) {
                    spot.doUpdate();
                }
            } else {
                insideTime = 0;
            }
        }

        public void setIsRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        // the main loop
        @Override
        public void run() {

            lastTime = System.currentTimeMillis();

            while (isRunning) {

                // grab hold of the canvas
                Canvas canvas = surfaceHolder.lockCanvas();

                if (canvas == null) {
                    // trouble -- exit nicely
                    isRunning = false;
                    continue;
                }

                synchronized (surfaceHolder) {

                    // compute how much time since last time around
                    long now = System.currentTimeMillis();
                    double elapsed = (now - lastTime) / 1000.0;
                    lastTime = now;

                    checkBall(elapsed);
                    // update/draw
                    doUpdate(elapsed);
                    doDraw(canvas, context);

                    //updateFPS(now);
                }

                // release the canvas
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        /* THE GAME */

        // move all objects in the game


        private void doUpdate(double elapsed) {


            ball.doUpdate(elapsed, y, x);
        }


        // draw all objects in the game
        private void doDraw(Canvas canvas, Context context) {
            // draw the background
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.board);
            canvas.drawBitmap(bitmap, null, new Rect(0, 0, (int) screenWidth, (int) screenHeight), null);
            spot.doDraw(canvas);
            ball.doDraw(canvas);


        }
    }
}


