package jp.co.mti.marun.android.stargazer;

/**
 * Created by maruyama_n on 2016/02/17.
 */
public abstract class SgDeviceManager {
    protected SgDeviceManager.Listener mListener;

    abstract public void connect();

    abstract public void disconnect();

    public void setListener(SgUsbSerialManager.Listener listener) {
        mListener = listener;
    }

    public SgUsbSerialManager.Listener getListener() {
        return this.mListener;
    }

    public void removeListener() {
        mListener = null;
    }

    public interface Listener {
        void onNewData(byte[] bytes);
        void onError(Exception e);
    }
}
