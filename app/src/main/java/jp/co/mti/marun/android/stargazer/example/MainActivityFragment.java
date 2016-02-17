package jp.co.mti.marun.android.stargazer.example;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import jp.co.mti.marun.android.stargazer.*;


public class MainActivityFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, StargazerManager.Listener {

    private final String TAG = this.getClass().getSimpleName();

    private StargazerManager mStargazerManager;
    private TextView mRawDataTextView;
    private TextView mDataTextview;
    private Switch mLoggingSwitch;
    private NavigationDisplayView mNavDisplay;
    private BufferedWriter mLogWriter = null;

    public MainActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mDataTextview = (TextView)view.findViewById(R.id.data_text);
        mRawDataTextView = (TextView)view.findViewById(R.id.raw_data_text);
        mLoggingSwitch = (Switch)view.findViewById(R.id.logging_switch);
        mLoggingSwitch.setOnCheckedChangeListener(this);
        mNavDisplay = (NavigationDisplayView)view.findViewById(R.id.navigation_display);

        SgDeviceManager sgDeviceManager = new SgUsbSerialManager(this.getActivity());

        //mStargazerManager = new StargazerNoDeviceDebugManager(StargazerNoDeviceDebugManager.TS_LORENZ);
        //mStargazerManager = new StargazerNoDeviceDebugManager(StargazerNoDeviceDebugManager.TS_RW);
        //mStargazerManager = new StargazerManager(usbSerialManager);
        mStargazerManager = new StargazerMultiIDManager(sgDeviceManager, new File(Environment.getExternalStorageDirectory() + "/stargazer/"));

        mStargazerManager.setListener(this);
        mStargazerManager.start();
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String toastMessage = "";
        if (isChecked) {
            try {
                File file = this.createNewLogFile();
                mLogWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
                mLogWriter.write("# unix_time[msec] id angle[degree] x[m] y[m] z[m]");
                mLogWriter.newLine();
                toastMessage = file.getPath();
            } catch (Exception e) {
                e.printStackTrace();
                mLogWriter = null;
                mLoggingSwitch.setChecked(false);
                toastMessage = e.getMessage();
            }
        } else {
            try {
                mLogWriter.flush();
                mLogWriter.close();
                mLogWriter = null;
                toastMessage = "Saved";
            } catch (IOException e) {
                e.printStackTrace();
                mLoggingSwitch.setChecked(true);
                toastMessage = e.getMessage();
            }
        }
        Toast.makeText(this.getActivity(), toastMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNewData(StargazerManager sm, final StargazerData data) {
        try {
            if (mLogWriter != null) {
                mLogWriter.write(data.toLogString());
                mLogWriter.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!data.isDeadZone) {
            mNavDisplay.setCurrentPoint(data);
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRawDataTextView.setText(data.rawDataString);
                mDataTextview.setText(data.toIdXYString());
            }
        });
    }

    @Override
    public void onError(StargazerManager sm, StargazerException e) {
        Log.e(TAG, e.getMessage());
    }

    private File createNewLogFile() {
        String filename = "/stargazer/";
        filename += DateFormat.format("yyyyMMdd-kkmmss", Calendar.getInstance());
        filename += ".log";

        String filePath = Environment.getExternalStorageDirectory() + filename;
        File file = new File(filePath);
        file.getParentFile().mkdir();
        return file;
    }
}
