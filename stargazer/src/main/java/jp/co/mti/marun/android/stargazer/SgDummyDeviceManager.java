package jp.co.mti.marun.android.stargazer;

import android.os.Handler;

/**
 * Created by maruyama_n on 2016/02/19.
 */
public class SgDummyDeviceManager extends SgDeviceManager implements Runnable {
    public static final String MULTI_ID_DUMMY_DATA = "MULTI_ID_DUMMY_DATA";
    public static final String LORENZ_ATTRACTOR = "LORENZ_ATTRACTOR";
    public static final String RANDOM_WALK = "RANDOM_WALK";

    private String dummyDataType;
    private Handler mHandler = new Handler();

    private int markerId = 65535;
    private double angle;
    private double x;
    private double y;
    private double z;

    // parameters
    private static final int DELAY = 100;

    // for dumy data


    // Lorenz attractor parameters
    private static final double p = 10;
    private static final double r = 28;
    private static final double b = 8.0 / 3.0;
    private static final double dt = 0.005;
    private static final double scale = 0.2;

    // Random walk parameter
    private static final double step_len = 0.1;


    public SgDummyDeviceManager(String type) {
        this.dummyDataType = type;
        this.initData();
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
        byte[] data = this.getData();
        this.callOnNewDataListener(data);
        this.updateData();
        mHandler.postDelayed(this, DELAY);
    }

    private void initData() {
        switch (this.dummyDataType) {
            case SgDummyDeviceManager.MULTI_ID_DUMMY_DATA:
                this.updateDummyData();
                break;
            case SgDummyDeviceManager.LORENZ_ATTRACTOR:
                angle = 0;
                x = Math.random();
                y = Math.random();
                z = Math.random();
                break;
            case SgDummyDeviceManager.RANDOM_WALK:
                angle = 0;
                x = 0;
                y = 0;
                z = 0;
                break;
            default:
                break;
        }
    }

    private void updateData() {
        switch (this.dummyDataType) {
            case SgDummyDeviceManager.MULTI_ID_DUMMY_DATA:
                this.updateDummyData();
                break;
            case SgDummyDeviceManager.LORENZ_ATTRACTOR:
                this.updateLorenzAttractor();
                break;
            case SgDummyDeviceManager.RANDOM_WALK:
                this.updateRandomWalk();
                break;
            default:
                break;
        }
    }

    private byte[] getData() {
        String dataStr;
        dataStr = String.format("~^I%d|%+.2f|%+.2f|%+.2f|%.2f`", markerId, angle, x*100, y*100, z*100);
        return dataStr.getBytes();
    }

    private void updateDummyData() {

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
