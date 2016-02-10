package jp.co.mti.marun.android.stargazer;

import android.os.Handler;

/**
 * Created by maruyama_n on 2016/02/10.
 */
public class StarGazerNoDeviceDebugManager extends StarGazerManager implements Runnable {
    private static final int DELAY = 100;
    private Handler mHandler = new Handler();
    private StarGazerData mPreviousData = null;

    // Lorenz atractor parameters
    private static float p = 10;
    private static float r = 28;
    private static float b = (float) (8.0 / 3.0);
    private static float scale = (float) 0.2;


    public StarGazerNoDeviceDebugManager() {
    }

    public void connect() {
        this.run();
    }

    public void disconnect() {
        mHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
        StarGazerData data;
        if (mPreviousData == null) {
            data = new StarGazerData();
            data.x = (float) Math.random();
            data.y = (float) Math.random();
            data.z = (float) Math.random();
        } else {
            data = this.generateNextData(mPreviousData);
        }
        callOnNewDataListener(data);
        mPreviousData = data;
        mHandler.postDelayed(this, DELAY);
    }

    private static StarGazerData generateNextData(StarGazerData previousData) {
        StarGazerData data = new StarGazerData();
        float preX = previousData.x / scale;
        float preY = previousData.y / scale;
        float preZ = previousData.z / scale;
        float dt = (float) ((data.time - previousData.time) * 0.00005);
        data.x = preX + dt * (-p * preX + p * preY);
        data.y = preY + dt * (-preX * preZ + r * preX - preY);
        data.z = preZ + dt * (preX * preY - b * preZ);
        data.x *= scale;
        data.y *= scale;
        data.z *= scale;
        return data;
    }
}