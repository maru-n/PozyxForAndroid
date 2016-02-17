package jp.co.mti.marun.android.stargazer;

import android.os.Handler;

/**
 * Created by maruyama_n on 2016/02/10.
 */
public class StargazerNoDeviceDebugManager extends StargazerManager implements Runnable {
    public static final int TS_LORENZ = 1;
    public static final int TS_RW     = 2;

    private int timeSeriesType;
    private static final int DELAY = 100;
    private Handler mHandler = new Handler();
    private StargazerData mPreviousData = null;

    // Lorenz attractor parameters
    private static final double p = 10;
    private static final double r = 28;
    private static final double b = 8.0 / 3.0;
    private static final double scale = 0.2;

    // Random walk parameter
    private static final double step_len = 0.1;


    public StargazerNoDeviceDebugManager(int timeSeriesType) {
        this.timeSeriesType = timeSeriesType;
    }

    public void connect() {
        this.run();
    }

    public void disconnect() {
        mHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
        StargazerData data;
        data = this.generateNextData(mPreviousData);
        callOnNewDataListener(data);
        mPreviousData = data;
        mHandler.postDelayed(this, DELAY);
    }

    private StargazerData generateNextData(StargazerData previousData) {
        switch (this.timeSeriesType) {
            case StargazerNoDeviceDebugManager.TS_LORENZ:
                return generateLorenzAttractor(previousData);
            case StargazerNoDeviceDebugManager.TS_RW:
                return generateRandomWalk(previousData);
            default:
                return new StargazerData();
        }
    }

    private static StargazerData generateLorenzAttractor(StargazerData previousData) {
        StargazerData data = new StargazerData();
        if (previousData == null) {
            data.x = Math.random();
            data.y = Math.random();
            data.z = Math.random();
        } else {
            double preX = previousData.x / scale;
            double preY = previousData.y / scale;
            double preZ = previousData.z / scale;
            double dt = (float) ((data.time - previousData.time) * 0.00005);
            data.x = preX + dt * (-p * preX + p * preY);
            data.y = preY + dt * (-preX * preZ + r * preX - preY);
            data.z = preZ + dt * (preX * preY - b * preZ);
            data.x *= scale;
            data.y *= scale;
            data.z *= scale;
        }
        return data;
    }

    private static StargazerData generateRandomWalk(StargazerData previousData) {
        StargazerData data = new StargazerData();
        if (previousData == null) {
            data.x = 0.0;
            data.y = 0.0;
            data.z = 0.0;
        } else {
            double th = Math.random() * Math.PI * 2.0;
            data.x = previousData.x + step_len * Math.cos(th);
            data.y = previousData.y + step_len * Math.sin(th);
            data.z = previousData.z;
        }
        return data;
    }
}