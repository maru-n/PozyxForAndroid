package jp.co.mti.marun.android.stargazer;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;

/**
 * Created by maruyama_n on 2016/02/10.
 */
public class StargazerMultiIDManager extends StargazerManager {

    public HashMap<Integer, double[]> markerMap = new HashMap<Integer, double[]>();

    public StargazerMultiIDManager(SgDeviceManager deviceManager, Reader markerMapData) {
        super(deviceManager);
        setupMarkerMap(markerMapData);
    }

    private void setupMarkerMap(Reader markerMapData) {
        BufferedReader data = new BufferedReader(markerMapData);
        String str = null;
        try {
            str = data.readLine();
            while(str != null){
                System.out.println(str);
                str = data.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
