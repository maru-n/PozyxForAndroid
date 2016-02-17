package jp.co.mti.marun.android.stargazer;

/**
 * Created by maruyama_n on 2015/12/21.
 */
public class StargazerException extends Exception {
    public StargazerException(String s) {
        super(s);
    }
    public StargazerException(Exception e) { super(e); }
    public StargazerException(String s, Exception e) {
        super(s, e);
    }
}
