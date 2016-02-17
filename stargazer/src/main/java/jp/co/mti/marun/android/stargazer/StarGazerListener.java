package jp.co.mti.marun.android.stargazer;

/**
 * Created by maruyama_n on 2015/12/21.
 */
public interface StargazerListener {
    void onNewData(StargazerManager sm, final StargazerData data);
    void onError(StargazerManager sm, final StargazerException e);
}
