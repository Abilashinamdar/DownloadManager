package inamdar.abilash.downloadmanager;

import android.os.Parcelable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static inamdar.abilash.downloadmanager.DownloadState.DOWNLOADING;

/**
 * Created by Abilash on 1/8/2017.
 */

public class DownloadRequest {

    public String url;
    public long id;
    public int state = DOWNLOADING;
    public DownloadStatusListener statusListener;

    // manually initializing properties.
    public int networkType;
    public String appDir;

    public DownloadRequest(long id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getFilePath() {
        String path = "";
        try {
            URL fileUrl = new URL(url);
            path = fileUrl.getPath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return path;
    }

    public String getFileName() {
        String name = "";
        try {
            URL fileUrl = new URL(url);
            name = fileUrl.getPath();
            name = name.substring(name.lastIndexOf('/') + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return name;
    }

    public void setDownloadStatusListener(DownloadStatusListener listener) {
        this.statusListener = listener;
    }

}
