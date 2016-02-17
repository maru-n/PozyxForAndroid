package jp.co.mti.marun.android.stargazer;

import android.content.Context;

import java.io.File;
import java.util.HashMap;

/**
 * Created by maruyama_n on 2016/02/10.
 */
public class StargazerMultiIDManager extends StargazerManager {

    public HashMap<Integer, double[]> markerData = new HashMap<Integer, double[]>();

    public StargazerMultiIDManager(Context context, File mapFile) {
        super(context);
        double[] data1 = {0.0, 0.0, 0.0, 0.0};
        this.markerData.put(24836, data1);
        double[] data2 = {0.0, 2.0, 0.0, 0.0};
        this.markerData.put(25094, data2);
        double[] data3 = {0.0, 0.0, -2.0, 0.0};
        this.markerData.put(24594, data3);
        double[] data4 = {0.0, 2.0, -2.0, 0.0};
        this.markerData.put(24706, data4);
    }

    @Override
    protected StargazerData makeStarGazerDataByOutput(String outputData) throws StargazerException {
        return new StargazerData(outputData, this.markerData);
    }
}
