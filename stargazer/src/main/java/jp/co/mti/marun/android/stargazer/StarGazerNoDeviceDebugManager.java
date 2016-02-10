package jp.co.mti.marun.android.stargazer;

import android.os.Handler;

/**
 * Created by maruyama_n on 2016/02/10.
 */
public class StarGazerNoDeviceDebugManager extends StarGazerManager implements Runnable {
    private static final int DELAY = 100;
    private Handler mHandler = new Handler();
    private StarGazerData mPreviousData = null;


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
            data = new StarGazerVirtualData();
        } else {
            data = StarGazerVirtualData.generateNextData(mPreviousData);
        }
        callOnNewDataListener(data);
        mPreviousData = data;
        mHandler.postDelayed(this, DELAY);
    }
}
