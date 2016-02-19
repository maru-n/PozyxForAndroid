package jp.co.mti.marun.android.stargazer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by maruyama_n on 2016/02/10.
 */
public class StargazerMultiIDManager extends StargazerManager {

    private final String TAG = this.getClass().getSimpleName();

    public HashMap<Integer, double[]> markerMap = new HashMap<Integer, double[]>();

    public StargazerMultiIDManager(SgDeviceManager deviceManager, InputStream markerMapDataStream) {
        super(deviceManager);
        setupMarkerMap(markerMapDataStream);
    }

    private void setupMarkerMap(InputStream markerMapDataStream) {
        BufferedReader stream = new BufferedReader(new InputStreamReader(markerMapDataStream));
        String line;
        try {
            while (( line = stream.readLine()) != null) {
                Log.d(TAG, line);
            }
        } catch (IOException e) {
            return;
        }

        double[] data1 = {0.0, 0.0, 0.0, 0.0};
        this.markerMap.put(24836, data1);
        double[] data2 = {0.0, 1.5, 0.0, 0.0};
        this.markerMap.put(25092, data2);
        double[] data3 = {0.0, 0.0, -1.5, 0.0};
        this.markerMap.put(24594, data3);
        double[] data4 = {0.0, 1.5, -1.5, 0.0};
        this.markerMap.put(24706, data4);
    }

    @Override
    protected StargazerData makeStarGazerDataByOutput(String outputData) throws StargazerException {
        return new StargazerData(outputData, this.markerMap);
    }
}
