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
import java.util.NoSuchElementException;

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

    private static final float POSITION_MARKER_RADIUS = 0.1F;
    private static final float TRACK_MARKER_RADIUS = 0.05F;
    private static final float LANDMARK_MARKER_RADIUS = 0.05F;

    private static final float DEFAULT_DISPLAY_RANGE = 10F;
    private static final float DISPLAY_MARGIN = 1.5F;

    private float displayXmin, displayXmax, displayYmin, displayYmax;
    private float displayScale;

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
        mPositionPaint.setStyle(Paint.Style.FILL);

        mTrackPaint = new Paint();
        mTrackPaint.setColor(Color.CYAN);
        mTrackPaint.setStyle(Paint.Style.FILL);

        mGridPaint = new Paint();
        mGridPaint.setColor(Color.LTGRAY);
        mGridPaint.setStrokeWidth(0);

        mAxisXPaint = new Paint();
        mAxisXPaint.setColor(Color.RED);
        mAxisXPaint.setStrokeWidth(0.05F);

        mAxisYPaint = new Paint();
        mAxisYPaint.setColor(Color.GREEN);
        mAxisYPaint.setStrokeWidth(0.05F);

        mLandmarkPaint = new Paint();
        mLandmarkPaint.setColor(Color.rgb(255,100,100));
        mLandmarkPaint.setStyle(Paint.Style.FILL);

        mLandmarkConnectPaint = new Paint();
        mLandmarkConnectPaint.setColor(Color.rgb(255,100,100));
        mLandmarkConnectPaint.setStrokeWidth(0);

        mDataList = new LinkedList<StargazerData>();
    }

    private void initCoordinate() {

        if (markerMap == null) {
            displayXmin = -DEFAULT_DISPLAY_RANGE/2;
            displayXmax = DEFAULT_DISPLAY_RANGE/2;
            displayYmin = -DEFAULT_DISPLAY_RANGE/2;
            displayYmax = DEFAULT_DISPLAY_RANGE/2;
        } else {
            displayXmin = -DISPLAY_MARGIN;
            displayXmax = DISPLAY_MARGIN;
            displayYmin = -DISPLAY_MARGIN;
            displayYmax = DISPLAY_MARGIN;
            for(Entry<Integer, double[]> marker : markerMap.entrySet()) {
                displayXmin = (float) Math.min(marker.getValue()[1]-DISPLAY_MARGIN, displayXmin);
                displayXmax = (float) Math.max(marker.getValue()[1]+DISPLAY_MARGIN, displayXmax);
                displayYmin = (float) Math.min(marker.getValue()[2]-DISPLAY_MARGIN, displayYmin);
                displayYmax = (float) Math.max(marker.getValue()[2]+DISPLAY_MARGIN, displayYmax);
            }
        }
        float displayXRange = displayXmax - displayXmin;
        float displayYRange = displayYmax - displayYmin;
        float scaleX = this.getWidth()/displayXRange;
        float scaleY = this.getHeight()/displayYRange;
        if (scaleX < scaleY) {
            displayScale = scaleX;
            float yBlank = this.getHeight()/ displayScale - displayYRange;
            displayYmin -= yBlank/2;
            displayYmax += yBlank/2;
        } else {
            displayScale = scaleY;
            float xBlank = this.getWidth()/ displayScale - displayXRange;
            displayXmin -= xBlank/2;
            displayXmax += xBlank/2;
        }
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
        Canvas canvas = mHolder.lockCanvas();
        mHolder.unlockCanvasAndPost(canvas);
        mLooper = new Thread(this);
        mLooper.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCoordinate();
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

        Canvas canvas = mHolder.lockCanvas();
        if (canvas == null) {return;}

        canvas.scale(displayScale, -displayScale);
        canvas.translate(-displayXmin, -displayYmax);

        canvas.drawColor(Color.WHITE);

        for (int x = (int) Math.floor(displayXmin); x <= displayXmax; x++) {
            canvas.drawLine(x, displayYmin, x, displayYmax, mGridPaint);
        }
        for (int y = (int) Math.floor(displayYmin); y <= displayYmax; y++) {
            canvas.drawLine(displayXmin, y, displayXmax, y, mGridPaint);
        }

        canvas.drawLine(0, 0, 1, 0, mAxisXPaint);
        canvas.drawLine(0, 0, 0, 1, mAxisYPaint);


        synchronized (mDataList) {

            if (this.markerMap != null) {
                for(Entry<Integer, double[]> marker : markerMap.entrySet()) {
                    float marker_x = (float) marker.getValue()[1];
                    float marker_y = (float) marker.getValue()[2];
                    canvas.drawCircle(marker_x, marker_y, LANDMARK_MARKER_RADIUS, mLandmarkPaint);
                    try {
                        StargazerData latestData = mDataList.getLast();
                        if (latestData.markerId[0] == marker.getKey()) {
                            canvas.drawLine(marker_x, marker_y, (float) latestData.x, (float) latestData.y, mLandmarkConnectPaint);
                        }
                        if (latestData.markerId[1] == marker.getKey()) {
                            canvas.drawLine(marker_x, marker_y, (float) latestData.x, (float) latestData.y, mLandmarkConnectPaint);
                        }
                    } catch (NoSuchElementException e) {}
                }
            }

            Iterator<StargazerData> iterator = mDataList.iterator();
            while (iterator.hasNext()) {
                StargazerData d = iterator.next();
                canvas.drawCircle((float)d.x, (float)d.y, TRACK_MARKER_RADIUS, mTrackPaint);
                if (d == mDataList.getLast()) {
                    canvas.drawCircle((float) d.x, (float) d.y, POSITION_MARKER_RADIUS, mPositionPaint);
                }
            }
        }

        mHolder.unlockCanvasAndPost(canvas);
    }
}

