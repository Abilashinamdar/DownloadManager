package inamdar.abilash.downloadmanager;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Abilash on 1/28/2017.
 */

public class Util {

    private static final String UNCONFIRMED = ".unconfirmed.download";


    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    public static File getDestFile(String appBasePath, DownloadRequest downloadRequest) {
        String filePath = downloadRequest.getFilePath();

        StringBuilder builder = new StringBuilder(appBasePath)
                .append("/")
                .append(filePath)
                .append("/")
                .append(downloadRequest.getFileName());

        File file = new File(builder.toString());
        file.getParentFile().mkdirs();
        return file;
    }

    public static File getPartiallyDownloadedFile(String appBasePath, DownloadRequest downloadRequest) {
        String parentPath = new File(downloadRequest.getFilePath()).getParent();

        StringBuilder builder = new StringBuilder(appBasePath)
                .append("/")
                .append(parentPath)
                .append("/")
                .append(downloadRequest.getFileName())
                .append(downloadRequest.id)
                .append(UNCONFIRMED);

        File file = new File(builder.toString());
        file.getParentFile().mkdirs();
        return file;
    }
}
