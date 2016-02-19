package jp.co.mti.marun.android.stargazer;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by maruyama_n on 2016/02/19.
 */
public class SgDummyDeviceManager extends SgDeviceManager implements Runnable {
    private final String TAG = this.getClass().getSimpleName();

    public static final String DUMMY_DATA = "DUMMY_DATA";
    public static final String LORENZ_ATTRACTOR = "LORENZ_ATTRACTOR";
    public static final String RANDOM_WALK = "RANDOM_WALK";
    public static final String STOP_ORIGIN = "STOP_ORIGIN";

    private String mDummyType;
    private Handler mHandler = new Handler();

    // parameters
    private static final int DELAY = 100;

    // for dummy data
    private ArrayList<String> mDummyDataList = new ArrayList<String>();
    private int mDummyDataIndex = 0;

    // for virtual data generate
    private int markerId;
    private double angle;
    private double x;
    private double y;
    private double z;

    // Lorenz attractor parameters
    private static final double p = 10;
    private static final double r = 28;
    private static final double b = 8.0 / 3.0;
    private static final double dt = 0.005;
    private static final double scale = 0.2;

    // Random walk parameter
    private static final double step_len = 0.1;


    public SgDummyDeviceManager(String type) {
        this.mDummyType = type;
        this.initData();
    }

    public SgDummyDeviceManager(String type, File dummyDataFile) {
        this(type);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dummyDataFile));
            String line;
            while (( line = reader.readLine()) != null) {
                mDummyDataList.add(line);
            }
            if (mDummyDataList.size() == 0) {
                throw new Exception("No dummy data.");
            }
        } catch (Exception e) {
            this.mDummyType = STOP_ORIGIN;
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void connect() {
        this.run();
    }

    @Override
    public void disconnect() {
        mHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
        byte[] data = new byte[0];
        try {
            data = this.getNextData();
            this.callOnNewDataListener(data);
        } catch (Exception e) {
            this.callOnErrorListener(e);
        }
        mHandler.postDelayed(this, DELAY);
    }

    private void initData() {
        if (mDummyType == LORENZ_ATTRACTOR) {
            markerId = 65535;
            angle = 0;
            x = Math.random();
            y = Math.random();
            z = Math.random();
        } else {
            markerId = 65535;
            angle = 0;
            x = 0;
            y = 0;
            z = 0;
        }
    }


    private byte[] getNextData() throws StargazerException, IOException {
        String dataStr = "";
        switch (mDummyType) {
            case DUMMY_DATA:
                dataStr = mDummyDataList.get(mDummyDataIndex++);
                if (mDummyDataList.size() <= mDummyDataIndex) {
                    mDummyDataIndex = 0;
                }
                break;

            case LORENZ_ATTRACTOR:
                this.updateLorenzAttractor();
            case RANDOM_WALK:
                this.updateRandomWalk();
            case STOP_ORIGIN:
                dataStr = String.format("~^I%d|%+.2f|%+.2f|%+.2f|%.2f`", markerId, angle, x*100, y*100, z*100);
                break;

            default:
                break;
        }
        return dataStr.getBytes();
    }

    private void updateLorenzAttractor() {
        double preX = x / scale;
        double preY = y / scale;
        double preZ = z / scale;
        x = preX + dt * (-p * preX + p * preY);
        y = preY + dt * (-preX * preZ + r * preX - preY);
        z = preZ + dt * (preX * preY - b * preZ);
        x *= scale;
        y *= scale;
        z *= scale;
    }

    private void updateRandomWalk() {
        double th = Math.random() * Math.PI * 2.0;
        this.x += step_len * Math.cos(th);
        this.y += step_len * Math.sin(th);
    }
}
