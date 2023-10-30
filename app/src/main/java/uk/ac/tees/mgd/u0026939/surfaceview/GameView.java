package uk.ac.tees.mgd.u0026939.surfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private SurfaceHolder surfaceHolder;
    private volatile boolean playing;
    private Canvas canvas;
    private Bitmap bitmap;
    private boolean isMoving;
    private float velocity = 250; // 250 px/s
    private float xPos = 10, yPos = 10;
    private int frameW = 115, frameH = 137;
    private int frameCount = 8;
    private int currentFrame = 0;
    private long fps;
    private long timeThisFrame;
    private long lastFrameChangeTime = 0;
    private int frameLengthInMS = 100;
    private Rect frameToDraw = new Rect(0,0,frameW,frameH);
    private RectF whereToDraw = new RectF(xPos, yPos, xPos + frameW, frameH);

    private boolean readyToUpdate = false;


    public GameView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.run);
        bitmap = Bitmap.createScaledBitmap(bitmap, frameW * frameCount, frameH, false);
    }


    @Override
    public void run() {
        while (playing)
        {
            long startFrameTime = System.currentTimeMillis();
            update();
            draw();
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1)
            {
                fps = 1000 / timeThisFrame;
            }
        }
    }

    private void update() {
        if (readyToUpdate) {
            if (isMoving) {
                xPos = xPos + velocity / fps;
                if (xPos > getWidth()) {
                    yPos += frameH;
                    xPos = 10;
                }
                if (yPos + frameH > getHeight()) {
                    yPos = 10;
                }
            }
        }
    }

    public void manageCurrentFrame()
    {
        long time = System.currentTimeMillis();
        if (isMoving)
        {
            if (time > lastFrameChangeTime + frameLengthInMS)
            {
                lastFrameChangeTime = time;
                currentFrame++;

                if (currentFrame >= frameCount)
                {
                    currentFrame = 0;
                }
            }
        }
        frameToDraw.left = currentFrame * frameW;
        frameToDraw.right = frameToDraw.left + frameW;
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid())
        {
            readyToUpdate = true;
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            whereToDraw.set(xPos, yPos, xPos+frameW, yPos + frameH);
            manageCurrentFrame();
            canvas.drawBitmap(bitmap, frameToDraw, whereToDraw, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause()
    {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("GameView", "Interrupted");
        }
    }

    public void resume()
    {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                isMoving = !isMoving;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("GameView","Action was MOVE: " + event.getX() + " " + event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                Log.d("GameView","Action was UP");
                break;
        }
        event.getX();
        return true;
    }
}
