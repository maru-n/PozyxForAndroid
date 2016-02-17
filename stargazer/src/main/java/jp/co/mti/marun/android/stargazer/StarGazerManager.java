package jp.co.mti.marun.android.stargazer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maruyama_n on 2015/12/21.
 */
public class StargazerManager implements SerialInputOutputManager.Listener {

    private static final String ACTION_USB_PERMISSION = "jp.co.mti.marun.stargazer.USB_PERMISSION";
    private static final int BAUD_RATE = 115200;
    private final String TAG = this.getClass().getSimpleName();
    private final Pattern OutputPattern = Pattern.compile("~(.+?)`");


    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent;
    private SerialInputOutputManager mSerialIoManager = null;
    private StargazerListener mListener = null;
    private StringBuffer buffer = new StringBuffer();

    private final BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null){
                        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
                        UsbSerialPort port = driver.getPorts().get(0);
                        try {
                            StargazerManager.this.openSerialIOPort(port);
                        } catch (StargazerException e) {
                            Log.w(TAG, e.getMessage());
                        }
                    }
                    else {
                        StargazerManager.this.callOnErrorListener(new StargazerException("permission denied for device " + device));
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                StargazerManager.this.connect();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            }
        }
    };

    public StargazerManager() {}

    public StargazerManager(Context context) {
        mUsbManager = (UsbManager) context.getSystemService(context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(mUsbPermissionReceiver, filter);
    }

    public void connect() {
        UsbSerialPort port = null;
        try {
            port = findDefaultPort();
        } catch (StargazerException e) {
            this.callOnErrorListener(e);
            return;
        }
        UsbDevice device = port.getDriver().getDevice();
        if (mUsbManager.hasPermission(device)) {
            try {
                openSerialIOPort(port);
            } catch (StargazerException e) {
                this.callOnErrorListener(e);
            }
        } else {
            mUsbManager.requestPermission(device, mPermissionIntent);
        }
    }

    public void disconnect() {
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
        }
    }

    public void setListener(StargazerListener listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }

    private UsbSerialPort findDefaultPort() throws StargazerException {
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            StargazerException e = new StargazerException("no available drivers.");
            Log.d(TAG, e.getMessage());
            throw e;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbSerialPort port = driver.getPorts().get(0);

        return port;
    }

    private void openSerialIOPort(UsbSerialPort port) throws StargazerException {
        UsbDeviceConnection connection = mUsbManager.openDevice(port.getDriver().getDevice());
        if (connection == null) {
            StargazerException e = new StargazerException("Opening device failed.");
            throw e;
        }

        try {
            port.open(connection);
            port.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            try {
                port.close();
            } catch (IOException e2) {
                throw new StargazerException(e2);
            }
            throw new StargazerException(e);
        }
        Log.d(TAG, "Serial device: " + port.getClass().getSimpleName());

        mSerialIoManager = new SerialInputOutputManager(port, this);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(mSerialIoManager);
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
    public void onRunError(Exception e) {
        Log.d(TAG, "Runner stopped.");
        StargazerException sge = new StargazerException("SerialInputOutputManager error.", e);
        callOnErrorListener(sge);
    }
}
