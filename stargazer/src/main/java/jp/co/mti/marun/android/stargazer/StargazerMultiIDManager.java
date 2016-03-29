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

    // <Marker_ID, {angle, x, y, z}>
    private HashMap<Integer, double[]> markerMap = new HashMap<Integer, double[]>();

    public StargazerMultiIDManager(SgDeviceManager deviceManager, InputStream markerMapDataStream) {
        super(deviceManager);
        setupMarkerMap(markerMapDataStream);
    }

    public HashMap<Integer, double[]> getMarkerMap(){
        return this.markerMap;
    }

    private void setupMarkerMap(InputStream markerMapDataStream) {
        BufferedReader stream = new BufferedReader(new InputStreamReader(markerMapDataStream));
        String line;
        try {
            while (( line = stream.readLine()) != null) {
                String strs[] = line.trim().split("[\\s]+");
                if (strs[0].substring(0,1).equals("#")) {
                    continue;
                }
                int markerId = Integer.parseInt(strs[0]);
                double angle = Double.parseDouble(strs[1]);
                double x = Double.parseDouble(strs[2]);
                double y = Double.parseDouble(strs[3]);
                double z = Double.parseDouble(strs[4]);
                double[] data = {angle, x, y, z};
                this.markerMap.put(markerId, data);
            }
        } catch (IOException e) {
            Log.e(TAG, "Invalid marker map file format.");
        }
    }

    @Override
    protected StargazerData makeStarGazerDataByOutput(String outputData) throws StargazerException {
        return new StargazerData(outputData, this.markerMap);
    }
}
