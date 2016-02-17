package jp.co.mti.marun.android.stargazer;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maruyama_n on 2015/12/18.
 */
public class StargazerData {
    public long time;
    public double angle;
    public double x;
    public double y;
    public double z;
    public int markerId;
    public boolean isDeadZone = false;
    public String rawDataString;

    private static final String DataPattern = "([0-9]+)\\|([\\+\\-][0-9]+\\.?[0-9]+)\\|([\\+\\-][0-9]+\\.?[0-9]+)\\|([\\+\\-][0-9]+\\.?[0-9]+)\\|([0-9]+\\.?[0-9]+)";
    private static final Pattern SingleIDDataPattern = Pattern.compile("~\\^I"+DataPattern+"`");
    private static final Pattern MultiIDDataPattern1 = Pattern.compile("~\\^1"+DataPattern+"`");
    private static final Pattern MultiIDDataPattern2 = Pattern.compile("~\\^2"+DataPattern+"\\|"+DataPattern+"`");
    private static final Pattern DeadZonePattern = Pattern.compile("~\\*DeadZone`");

    public StargazerData(){
        this.time = System.currentTimeMillis();
    }

    public StargazerData(String rawData) throws StargazerException {
        this();
        this.rawDataString = rawData;
        Matcher m = SingleIDDataPattern.matcher(rawData);
        if (m.find()) {
            this.markerId = Integer.parseInt(m.group(1));
            this.angle = Float.parseFloat(m.group(2));
            this.x = (float) (Float.parseFloat(m.group(3)) * 0.01);
            this.y = (float) (Float.parseFloat(m.group(4)) * 0.01);
            this.z = (float) (Float.parseFloat(m.group(5)) * 0.01);
        } else if (DeadZonePattern.matcher(rawData).find()) {
            this.isDeadZone = true;
        } else{
            throw new StargazerException("Invalid data format.");
        }
    }

    public StargazerData(String rawData, HashMap<Integer, double[]> markerData) throws StargazerException {
        this();
        this.rawDataString = rawData;
        Matcher m1 = MultiIDDataPattern1.matcher(rawData);
        Matcher m2 = MultiIDDataPattern2.matcher(rawData);
        if (m1.find()) {
            int markerId = Integer.parseInt(m1.group(1));
            double angle = Float.parseFloat(m1.group(2));
            double x = (float) (Float.parseFloat(m1.group(3)) * 0.01);
            double y = (float) (Float.parseFloat(m1.group(4)) * 0.01);
            double z = (float) (Float.parseFloat(m1.group(5)) * 0.01);
            this.markerId = markerId;
            this.angle = angle;
            this.x = x;
            this.y = y;
            this.z = z;
        } else if (m2.find()) {
            int markerId1 = Integer.parseInt(m2.group(1));
            double angle1 = Float.parseFloat(m2.group(2));
            double x1 = (float) (Float.parseFloat(m2.group(3)) * 0.01);
            double y1 = (float) (Float.parseFloat(m2.group(4)) * 0.01);
            double z1 = (float) (Float.parseFloat(m2.group(5)) * 0.01);
            int markerId2 = Integer.parseInt(m2.group(6));
            double angle2 = Float.parseFloat(m2.group(7));
            double x2 = (float) (Float.parseFloat(m2.group(8)) * 0.01);
            double y2 = (float) (Float.parseFloat(m2.group(9)) * 0.01);
            double z2 = (float) (Float.parseFloat(m2.group(10)) * 0.01);
            this.markerId = markerId2;
            this.angle = angle2;
            this.x = x2;
            this.y = y2;
            this.z = z2;
        } else if (DeadZonePattern.matcher(rawData).find()) {
            this.isDeadZone = true;
        } else {
            throw new StargazerException("Invalid data format.");
        }
    }

    public String toXYString() {
        if (this.isDeadZone) {
            return String.format("time:%d DeadZone", this.time);
        } else {
            return String.format("time:%d x:%.4f  y:%.4f", this.time, this.x, this.y);
        }
    }

    public String toIdXYString() {
        if (this.isDeadZone) {
            return String.format("time:%d\nDeadZone", this.time);
        } else {
            return String.format("time:%d\nid:%d x:%.4f  y:%.4f", this.time, this.markerId, this.x, this.y);
        }
    }

    public String toLogString() {
        if (this.isDeadZone) {
            return String.format("# %d DeadZone", this.time);
        } else {
            return String.format("%d %d %.2f %.4f %.4f %.4f", this.time, this.markerId, this.angle, this.x, this.y, this.z);
        }
    }
}