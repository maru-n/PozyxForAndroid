package jp.co.mti.marun.android.stargazer;

/**
 * Created by maruyama_n on 2016/02/17.
 */
public abstract class SgDeviceManager {
    protected SgDeviceManager.Listener mListener;

    abstract public void connect();

    abstract public void disconnect();

    public void setListener(SgUsbSerialDeviceManager.Listener listener) {
        mListener = listener;
    }

    public SgUsbSerialDeviceManager.Listener getListener() {
        return this.mListener;
    }

    public void removeListener() {
        mListener = null;
    }

    protected void callOnNewDataListener(byte[] data) {
        if (mListener != null) {
            mListener.onNewData(data);
        }
    }

    protected void callOnErrorListener(Exception e) {
        if (mListener != null) {
            mListener.onError(e);
        }
    }

    public interface Listener {
        void onNewData(byte[] data);
        void onError(Exception e);
    }
}
