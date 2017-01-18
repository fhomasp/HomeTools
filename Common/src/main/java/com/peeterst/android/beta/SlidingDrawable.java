
package com.peeterst.android.beta;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.ViewGroup;
import android.view.WindowManager;


/**
 *     http://www.anddev.org/view-layout-resource-problems-f27/scrolling-background-t55503.html?sid=2141c422104ef659c4ddd99babe7fa6d
 */
public class SlidingDrawable extends Drawable implements Drawable.Callback {
    private static final String TAG = "SlidingDraw";
    private static float STEP_SIZE = 1.5f;

    private BitmapDrawable mBitmap;
    private Context mContext;

    private float mPosX;
    private int mBitmapWidth;

    private Runnable mInvalidater;
    private Handler mHandler;

    public SlidingDrawable(Context c){
        mContext = c;

        // use this as the callback as we're implementing the interface
        setCallback(this);

        mHandler = new Handler();
        mInvalidater = new Runnable(){

            public void run(){
                // decrement the drawables step size
                mPosX -= SlidingDrawable.STEP_SIZE;

                /*
                     * Check to see if the current position is at point where it should
                     * loop.  If so, reset back to 0 to restart
                     */
                if(Math.abs(mPosX) >= mBitmapWidth)
                    mPosX = 0;

                // redraw
                invalidateDrawable(SlidingDrawable.this);
            }
        };
    }
    public static void setStepSize(float newSize){
        SlidingDrawable.STEP_SIZE = newSize;
    }
    public void createBitmap(String path, ViewGroup parent){
        // height of the parent container
        int height = parent.getHeight();

        /* Initialize local variables
           * 	bgBitmap 	- the resulting bitmap to send into SlidingDrawable instance
           * 	imageStream	- raw bitmap data to be decoded into bgBitmap
           */
        WindowManager wMgr = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        int mScreenWidth = wMgr.getDefaultDisplay().getWidth();

        InputStream imageStream;
        Matrix imgMatrix = new Matrix();
        Bitmap bitmap = null;
        try {
            imageStream = mContext.getAssets().open(path);

            // create a temporary bitmap object for basic data
            Bitmap temp = BitmapFactory.decodeStream(imageStream);
            int width = temp.getWidth();

            // find the width difference as a percentage to apply to the
            // transformation matrix
            float widthDifference = ((float)mScreenWidth) / (float)(width / 2);
            imgMatrix.postScale(widthDifference, 0, 0f, 0f);

            // create a copy of the bitmap, scaled correctly to maintain loop
            bitmap = Bitmap.createScaledBitmap(temp, (int)(width * widthDifference), height, true);

            // recycle the temp bitmap
            temp.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBitmap = new BitmapDrawable(bitmap);

        // required
        mBitmapWidth = getIntrinsicWidth() / 2;
        Rect bounds = new Rect(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
        setBounds(bounds);
    }
    @Override
    public void draw(Canvas canvas) {
        scheduleDrawable(this, mInvalidater, SystemClock.uptimeMillis() + 32);
        canvas.drawBitmap(mBitmap.getBitmap(), mPosX, 0f, null);
    }
    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        mHandler.postAtTime(what, who, when);
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        mHandler.removeCallbacks(what, who);
    }

    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }
    /*
      * Methods not directly used or called
      *
      */
    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmap.getBitmap().getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmap.getBitmap().getHeight();
    }

    @Override
    public int getMinimumWidth() {
        return mBitmap.getBitmap().getWidth();
    }

    @Override
    public int getMinimumHeight() {
        return mBitmap.getBitmap().getHeight();
    }
}


