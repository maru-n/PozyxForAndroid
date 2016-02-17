package jp.co.mti.marun.android.stargazer;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maruyama_n on 2015/12/21.
 */
public class StargazerManager implements SgUsbSerialManager.Listener {
    private final String TAG = this.getClass().getSimpleName();
    private final Pattern OutputPattern = Pattern.compile("~(.+?)`");

    private SgDeviceManager sgDeviceManager;
    private StargazerManager.Listener mListener = null;
    private StringBuffer buffer = new StringBuffer();

    public StargazerManager() {}

    public StargazerManager(SgDeviceManager deviceManager) {
        this.sgDeviceManager = deviceManager;
        this.sgDeviceManager.setListener(this);
    }

    public void start() {
        this.sgDeviceManager.connect();
    }

    public void stop() {
        this.sgDeviceManager.disconnect();
    }

    public void setListener(StargazerManager.Listener listener) {
        mListener = listener;
    }

    public StargazerManager.Listener getListener() {
        return this.mListener;
    }

    public void removeListener() {
        mListener = null;
    }

    protected void callOnNewDataListener(StargazerData d) {
        if (mListener != null) {
            mListener.onNewData(this, d);
        }
    }

    protected void callOnErrorListener(StargazerException e) {
        if (mListener != null) {
            mListener.onError(this, e);
        }
    }

    protected StargazerData makeStarGazerDataByOutput(String outputData) throws StargazerException {
        return new StargazerData(outputData);
    }

    @Override
    public void onNewData(byte[] bytes) {
        buffer.append(new String(bytes));
        String str = buffer.toString();
        Matcher m = OutputPattern.matcher(str);
        int lastMatchIndex = 0;
        while (m.find()) {
            final String line = m.group();
            try {
                final StargazerData data = this.makeStarGazerDataByOutput(line);
                callOnNewDataListener(data);
            } catch (StargazerException e) {
                callOnErrorListener(e);
            }
            lastMatchIndex = m.end();
        }
        buffer.delete(0, lastMatchIndex);
    }

    @Override
    public void onError(Exception e) {
        Log.d(TAG, "Runner stopped.");
        StargazerException sge = new StargazerException("SerialInputOutputManager error.", e);
        callOnErrorListener(sge);
    }

    public interface Listener {
        void onNewData(StargazerManager sm, final StargazerData data);
        void onError(StargazerManager sm, final StargazerException e);
    }
}
