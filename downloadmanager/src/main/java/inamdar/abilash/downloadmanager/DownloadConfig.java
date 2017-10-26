package inamdar.abilash.downloadmanager;

/**
 * Created by Abilash on 1/27/2017.
 */

public class DownloadConfig {
    public DownloadStatusListener statusListener = null;
    public int networkType = NetworkType.ALL;
    public boolean runInBackground = true;
    public String appDir = "";

    private static DownloadConfig mDownloadConfig;

    private DownloadConfig() { }



    public static DownloadConfig obtain() {
        if (mDownloadConfig == null) {
            mDownloadConfig = new DownloadConfig();
        }
        return mDownloadConfig;
    }


}
