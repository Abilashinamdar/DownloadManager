package inamdar.abilash.downloadmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by Abilash on 1/8/2017.
 */

public class DownloadManager {

    private static Context mContext;
    private static DownloadManager downloadManager;
    private DownloadConfig mDownloadConfig;

    private DownloadService mDownloadService;


    public static class Builder {
        private Context context;
        private String appDir;
        private DownloadStatusListener statusListener;
        private int networkType;
        private boolean runInBackground;


        public Builder(Context context, String appDir) {
            this.context = context;
            this.appDir = appDir;
        }

        public Builder runInBackground(boolean flag) {
            this.runInBackground = flag;
            return this;
        }

        public Builder setNetworkType(int type) {
            this.networkType = type;
            return this;
        }

        public Builder setDownloadListener(DownloadStatusListener listener) {
            this.statusListener = listener;
            return this;
        }

        public DownloadManager build() {
            DownloadConfig config = DownloadConfig.obtain();
            config.statusListener = statusListener;
            config.runInBackground = runInBackground;
            config.networkType = networkType;
            config.appDir = appDir;
            downloadManager = new DownloadManager(context, config);
            return downloadManager;
        }
    }

    /*public DownloadManager() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DownloadConstants.KEY_HANDLER, new Messenger(mHandler));
        mContext.startService(intent);
    }*/

    private DownloadManager(Context context, DownloadConfig downloadConfig) {
        mContext = context;
        mDownloadConfig = downloadConfig;

        Intent intent = new Intent(mContext, DownloadService.class);
        mContext.bindService(intent, getServiceConnection(intent), Context.BIND_AUTO_CREATE);
    }

    ServiceConnection mServiceConnection;

    private ServiceConnection getServiceConnection(final Intent service) {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                DownloadService.LocalBinder localBinder = (DownloadService.LocalBinder) binder;
                mDownloadService = localBinder.getService();
                mContext.startService(service);
                mContext.unbindService(mServiceConnection);
                System.out.println();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceConnection = null;
                System.out.println();
            }
        };
        return mServiceConnection;
    }


    /*public DownloadManager(String newAppDir, int newNetworkType, boolean newRunInBackground, DownloadStatusListener newStatusListener) {
        appDir = newAppDir;
        networkType = newNetworkType;
        runInBackground = newRunInBackground;
        statusListener = newStatusListener;
    }*/

    public void enqueue(DownloadRequest request) {
        mDownloadService.enqueue(request);
    }

    public void cancelAll() {
        mDownloadService.shutDown();
    }

    public void pause(long id) {
        mDownloadService.pause(id);
    }

    public void cancel(long id) {
        mDownloadService.cancel(id);
    }

    public void resume(long id) {
        mDownloadService.resume(id);
    }

    public static DownloadManager getInstance() {
        return downloadManager;
    }

    public DownloadConfig getConfig() {
        return mDownloadConfig;
    }


}
