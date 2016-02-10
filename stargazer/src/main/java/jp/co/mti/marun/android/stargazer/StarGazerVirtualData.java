package jp.co.mti.marun.android.stargazer;

public class StarGazerVirtualData extends StarGazerData {

    private static float p = 10;
    private static float r = 28;
    private static float b = (float) (8.0/3.0);
    private static float scale = (float) 0.2;

    public StarGazerVirtualData() {
        super();
        this.x = (float) Math.random();
        this.y = (float) Math.random();
        this.z = (float) Math.random();
    }

    static StarGazerVirtualData generateNextData(StarGazerData previousData) {
        StarGazerVirtualData data = new StarGazerVirtualData();
        float preX = previousData.x / scale;
        float preY = previousData.y / scale;
        float preZ = previousData.z / scale;
        float dt = (float) ((data.time - previousData.time)*0.00005);
        data.x = preX + dt * (-p*preX + p*preY);
        data.y = preY + dt * (-preX*preZ + r*preX - preY);
        data.z = preZ + dt * (preX*preY - b*preZ);
        data.x *= scale;
        data.y *= scale;
        data.z *= scale;
        return data;
    }
}
