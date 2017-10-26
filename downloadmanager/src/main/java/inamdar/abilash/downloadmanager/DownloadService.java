package inamdar.abilash.downloadmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Abilash on 1/15/2017.
 */

public final class DownloadService extends Service {

//    private final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final int NUMBER_OF_CORES = 1;
    private final int CORE_POOL_SIZE = NUMBER_OF_CORES;
    private final int MAX_POOL_SIZE = NUMBER_OF_CORES;
    private final int KEEP_ALIVE_TIME = 5000;
    private final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;
    private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(1);

    private LinkedHashMap<Long, DownloadRequest> mDownloadQueueMap = new LinkedHashMap<>();
    private ExecutorService mExecutorService;
    private final IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        DownloadService getService() {
            return DownloadService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("inside onCreate() of DownloadService");

        // initializing DB instance
        DBService.initialize(this);

        if(DownloadManager.getInstance() == null) {
            List<DownloadRequest> requests = DBService.getInstance().getAllRequestsOfDownloadState(DownloadState.DOWNLOADING);
            enqueue(requests);
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("inside onStartCommand() of DownloadService");

        if (null == mExecutorService) {
            mExecutorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, queue);
        }

        return DownloadManager.getInstance().getConfig().runInBackground ? START_STICKY : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        System.out.println("inside onDestroy() of DownloadService");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("inside onTaskRemoved() of DownloadService");
        super.onTaskRemoved(rootIntent);
    }

    public void enqueue(DownloadRequest request) {
        if(request == null) {
            return;
        }

        request = initialize(request);

        DownloadRequest downloadRequest = getRequestOfId(request.id);
        if(downloadRequest != null && downloadRequest.state != DownloadState.DOWNLOADING ) {
            request = downloadRequest;
        }

        request.state = DownloadState.DOWNLOADING;
        DownloadThread downloadThread = new DownloadThread(request);
        mExecutorService.submit(downloadThread);
        mDownloadQueueMap.put(request.id, request);
    }

    public void enqueue(List<DownloadRequest> requests) {
        for(DownloadRequest request : requests) {
            enqueue(request);
        }
    }

    private DownloadRequest initialize(DownloadRequest request) {
        DownloadConfig config = DownloadManager.getInstance().getConfig();
        if(config == null) {
            return request;
        }

        if(request.appDir == null) {
            request.appDir = config.appDir;
        }

        if(request.networkType == 0) {
            request.networkType = config.networkType;
        }

        return request;
    }

    public void pause(long id) {
        DownloadRequest downloadRequest = getRequestOfId(id);
        if (downloadRequest != null && downloadRequest.state == DownloadState.DOWNLOADING) {
            downloadRequest.state = DownloadState.PAUSE;
        }
    }

    public void cancel(long id) {
        DownloadRequest downloadRequest = getRequestOfId(id);
        if (downloadRequest != null) {
            downloadRequest.state = DownloadState.STOP;
        }
    }

    public void resume(long id) {
        DownloadRequest downloadRequest = getRequestOfId(id);
        if (downloadRequest != null && downloadRequest.state == DownloadState.PAUSE) {
            enqueue(downloadRequest);
        }
    }


    private DownloadRequest getRequestOfId(long id) {
        if (mDownloadQueueMap == null) {
            return null;
        }

        DownloadRequest downloadRequest = mDownloadQueueMap.get(id);
        if (downloadRequest == null) {
            return null;
        }

        return downloadRequest;

    }

    public void shutDown() {
        mExecutorService.shutdownNow();
    }

    public void storeRequests() {
        Set<Map.Entry<Long, DownloadRequest>> entries = mDownloadQueueMap.entrySet();
        for(Map.Entry entry : entries) {
            entry.getKey();
        }
    }
}
