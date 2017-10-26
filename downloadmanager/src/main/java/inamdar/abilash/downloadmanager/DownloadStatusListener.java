package inamdar.abilash.downloadmanager;

import java.io.IOException;

/**
 * Created by Abilash on 1/8/2017.
 */

public interface DownloadStatusListener {

    public void onDownloadProgress(long downloadRequestId, long downloadedBytes, long totalBytes);

    public void onDownloadPaused(long downloadRequestId);

    public void onDownloadStopped(long downloadRequestId, long downloadedBytes);

    public void onDownloadFinished(long downloadRequestId);

    public void onDownloadFailed(long downloadRequestId, Exception e);
}
