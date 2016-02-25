package jp.co.mti.marun.android.stargazer.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import jp.co.mti.marun.android.stargazer.StargazerData;

/**
 * Created by maruyama_n on 2015/12/18.
 */
public class NavigationDisplayView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Paint mPositionPaint, mTrackPaint, mGridPaint, mAxisXPaint, mAxisYPaint, mLandmarkPaint, mLandmarkConnectPaint;
    private SurfaceHolder mHolder;
    private Thread mLooper;
    private LinkedList<StargazerData> mDataList;
    private HashMap<Integer, double[]> markerMap;

    private static final int MAX_TRACK_DATA = 300;
    private static final int   GRID_UNIT_PIXEL = 50;
    private static final float GRID_UNIT_METER = 1;
    private static final int POSITION_MARKER_RADIUS = 10;
    private static final int LANDMARK_MARKER_RADIUS = 3;
    private static final int TRACK_MARKER_RADIUS = 3;
    private static final int ORIGIN_SHIFT_X_PIXEL= -255;
    private static final int ORIGIN_SHIFT_Y_PIXEL = 150;


    public NavigationDisplayView(Context context) {
        super(context);
        init();
    }

    public NavigationDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NavigationDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        mPositionPaint = new Paint();
        mPositionPaint.setColor(Color.BLUE);
        //mPositionPaint.setAntiAlias(true);
        mPositionPaint.setStyle(Paint.Style.FILL);

        mTrackPaint = new Paint();
        mTrackPaint.setColor(Color.CYAN);
        mTrackPaint.setStyle(Paint.Style.FILL);

        mGridPaint = new Paint();
        mGridPaint.setColor(Color.LTGRAY);

        mAxisXPaint = new Paint();
        mAxisXPaint.setColor(Color.RED);
        mAxisXPaint.setStrokeWidth(5);

        mAxisYPaint = new Paint();
        mAxisYPaint.setColor(Color.GREEN);
        mAxisYPaint.setStrokeWidth(5);

        mLandmarkPaint = new Paint();
        mLandmarkPaint.setColor(Color.RED);
        mLandmarkPaint.setStyle(Paint.Style.FILL);

        mLandmarkConnectPaint = new Paint();
        mLandmarkConnectPaint.setColor(Color.RED);
        mLandmarkConnectPaint.setStrokeWidth(1);

        mDataList = new LinkedList<StargazerData>();
    }

    public void setCurrentPoint(StargazerData data) {
        synchronized(mDataList) {
            mDataList.add(data);
            if (mDataList.size() > MAX_TRACK_DATA) {
                mDataList.removeFirst();
            }
        }
    }

    public void setMarkerMap(HashMap<Integer, double[]> markerMap) {
        this.markerMap = markerMap;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mLooper = new Thread(this);
        mLooper.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mLooper = null;
    }


    @Override
    public void run() {
        while (mLooper != null) {
            draw();
        }
    }

    private void draw() {
        int width = this.getWidth();
        int height = this.getHeight();
        int centerX = width/2 + ORIGIN_SHIFT_X_PIXEL;
        int centerY = height/2 - ORIGIN_SHIFT_Y_PIXEL;

        Canvas canvas = mHolder.lockCanvas();
        if (canvas == null) {return;}

        canvas.drawColor(Color.WHITE);
        for (int x=centerX%GRID_UNIT_PIXEL; x<width; x+=GRID_UNIT_PIXEL) {
            canvas.drawLine(x, 0, x, height, mGridPaint);
        }
        for (int y=centerY%GRID_UNIT_PIXEL; y<height; y+=GRID_UNIT_PIXEL) {
            canvas.drawLine(0, y, width, y, mGridPaint);
        }
        canvas.drawLine(centerX, centerY, centerX + GRID_UNIT_PIXEL, centerY, mAxisXPaint);
        canvas.drawLine(centerX, centerY, centerX, centerY - GRID_UNIT_PIXEL, mAxisYPaint);


        synchronized (mDataList) {
            StargazerData latestData = mDataList.getLast();

            if (this.markerMap != null) {
                for(Entry<Integer, double[]> marker : markerMap.entrySet()) {
                    double x = marker.getValue()[1];
                    double y = marker.getValue()[2];
                    canvas.drawCircle(centerX + convertMeter2Pixel(x), centerY - convertMeter2Pixel(y), LANDMARK_MARKER_RADIUS, mLandmarkPaint);

                    if (latestData.markerId[0] == marker.getKey()) {
                        canvas.drawLine(centerX + convertMeter2Pixel(latestData.x), centerY - convertMeter2Pixel(latestData.y),
                                centerX + convertMeter2Pixel(x), centerY - convertMeter2Pixel(y), mLandmarkConnectPaint);
                    }
                    if (latestData.markerId[1] == marker.getKey()) {
                        canvas.drawLine(centerX + convertMeter2Pixel(latestData.x), centerY - convertMeter2Pixel(latestData.y),
                                centerX + convertMeter2Pixel(x), centerY - convertMeter2Pixel(y), mLandmarkConnectPaint);
                    }
                }
            }

            Iterator<StargazerData> iterator = mDataList.iterator();
            while (iterator.hasNext()) {
                StargazerData d = iterator.next();
                if (d.equals(latestData)) {
                    canvas.drawCircle(centerX + convertMeter2Pixel(d.x), centerY - convertMeter2Pixel(d.y), POSITION_MARKER_RADIUS, mPositionPaint);
                } else {
                    canvas.drawCircle(centerX + convertMeter2Pixel(d.x), centerY - convertMeter2Pixel(d.y), TRACK_MARKER_RADIUS, mTrackPaint);
                }
            }
        }
        mHolder.unlockCanvasAndPost(canvas);
    }
    
    private int convertMeter2Pixel(double x) {
        int p = (int) (GRID_UNIT_PIXEL * x / GRID_UNIT_METER);
        return p;
    }
}

