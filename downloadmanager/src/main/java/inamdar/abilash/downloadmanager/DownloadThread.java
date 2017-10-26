package inamdar.abilash.downloadmanager;

import android.app.Application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static inamdar.abilash.downloadmanager.DownloadState.DOWNLOADING;
import static inamdar.abilash.downloadmanager.DownloadState.FAILED;
import static inamdar.abilash.downloadmanager.DownloadState.FINISH;
import static inamdar.abilash.downloadmanager.DownloadState.PAUSE;
import static inamdar.abilash.downloadmanager.DownloadState.STOP;

/**
 * Created by Abilash on 1/8/2017.
 */

public class DownloadThread implements Runnable {

    private DownloadRequest mDownloadRequest;

    public DownloadThread(DownloadRequest request) {
        mDownloadRequest = request;

        // store request in DB.
        DBService.getInstance().save(request);

    }

    private HttpURLConnection makeConnection(List<Header> headers) throws IOException {
        URL url = new URL(mDownloadRequest.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if (headers != null) {
            for (Header header : headers) {
                connection.setRequestProperty(header.name, header.value);
            }
        }
        connection.connect();
        return connection;
    }

    @Override
    public void run() {

        InputStream inputStream = null;
        FileOutputStream fos = null;
        List<Header> headerList = new ArrayList<>();
        String appBasePath = mDownloadRequest.appDir;
        try {

            File partialFile = Util.getPartiallyDownloadedFile(appBasePath, mDownloadRequest);
            if(partialFile != null && partialFile.exists()) {
                long length = partialFile.length();
                Header header = new Header();
                header.name = "Range";
                header.value = "bytes=" + length + "-";
                headerList.add(header);
            }

            HttpURLConnection connection = makeConnection(headerList);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            if(responseCode == 200 && partialFile.exists()) {
                Util.deleteFile(partialFile);
            }

            inputStream = connection.getInputStream();
            fos = new FileOutputStream(partialFile, true);
            boolean writeComplete = writeToFile(fos, inputStream, contentLength);
            if(writeComplete) {
//                Util.writeToFile(partialFile, Util.getDestFile(appBasePath, mDownloadRequest));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            changeDownloadStatus(FAILED, 0, 0, e);
        } catch (IOException e) {
            e.printStackTrace();
            changeDownloadStatus(FAILED, 0, 0, e);
        }
        /*finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    private boolean writeToFile(OutputStream os, InputStream inputStream, int totalBytes) throws IOException {
        int read = 0;
        int downloaded = 0;
        boolean completed = true;
        byte[] buffer = new byte[1024 * 5];

        while ((read = inputStream.read(buffer)) > 0) {
            if(mDownloadRequest.state == PAUSE) {
                completed = false;
                changeDownloadStatus(PAUSE, downloaded, totalBytes, null);
                break;
            }

            if(mDownloadRequest.state == STOP) {
                completed = false;
                changeDownloadStatus(STOP, downloaded, totalBytes, null);
                break;
            }
            os.write(buffer, 0, read);
            downloaded += read;
            changeDownloadStatus(DOWNLOADING, downloaded, totalBytes, null);
        }
        return completed;
    }

    private synchronized void changeDownloadStatus(int state, long downloadedBytes, long totalBytes, Exception e) {

        if(state != DOWNLOADING) {
            DBService.getInstance().updateState(mDownloadRequest.id, state);
        }

        DownloadConfig config = DownloadManager.getInstance().getConfig();

        if(config != null && config.statusListener != null) {
            changeDownloadStatus(state, config.statusListener, downloadedBytes, totalBytes, e);
        }

        if(mDownloadRequest.statusListener != null) {
            changeDownloadStatus(state, mDownloadRequest.statusListener, downloadedBytes, totalBytes, e);
        }
    }

    private void changeDownloadStatus(int state, DownloadStatusListener listener, long downloadedBytes, long totalBytes, Exception e) {
        switch (state) {
            case PAUSE:
                listener.onDownloadPaused(mDownloadRequest.id);
                break;
            case STOP:
                listener.onDownloadStopped(mDownloadRequest.id, downloadedBytes);
                break;
            case FINISH:
                listener.onDownloadFinished(mDownloadRequest.id);
                break;
            case DOWNLOADING:
                listener.onDownloadProgress(mDownloadRequest.id, downloadedBytes, totalBytes);
                break;
            case FAILED:
                listener.onDownloadFailed(mDownloadRequest.id, e);
                break;
        }
    }

}
