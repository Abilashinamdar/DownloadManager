package inamdar.abilash.downloadmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abilash on 5/13/2017.
 */

public class DBService extends SQLiteOpenHelper {

    private String TABLE = " download ";

    private String COLUMN_ID = " id ";
    private String COLUMN_REQUEST = " request ";
    private String COLUMN_STATE = " state ";

    private static DBService mDbService;

    private DBService(Context context) {
        super(context, "DownloadManager", null, 1);
    }

    public static DBService initialize(Context context) {
        if(mDbService == null) {
            mDbService = new DBService(context);
        }

        return getInstance();
    }

    public static DBService getInstance() {
        return mDbService;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        StringBuilder createTable = new StringBuilder();
        createTable.append("create table")
                .append(TABLE)
                .append("(")
                    .append(COLUMN_ID)
                    .append("integer primary key,")
                    .append(COLUMN_REQUEST)
                    .append("text,")
                    .append(COLUMN_STATE)
                    .append("integer")
                .append(")");


        db.execSQL(createTable.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void save(DownloadRequest request) {

        if(isExist(request.id)) {
            updateRequest(request);
            return;
        }

        Gson gson = new Gson();
        String data = gson.toJson(request);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, request.id);
        contentValues.put(COLUMN_REQUEST, data);
        contentValues.put(COLUMN_STATE, request.state);

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE, null, contentValues);
        db.close();
    }

    public boolean isExist(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "select " + COLUMN_ID + " from " + TABLE + " where " + COLUMN_ID + " = " + id;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor == null) {
            return false;
        }

        return cursor.getCount() > 0;
    }

    public DownloadRequest getRequestByID(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "select * from " + TABLE + " where " + COLUMN_ID + " = " + id;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor == null) {
            return null;
        }

        cursor.moveToFirst();
        String request = cursor.getString(cursor.getColumnIndex(COLUMN_REQUEST));
        DownloadRequest downloadRequest = new Gson().fromJson(request, DownloadRequest.class);
        downloadRequest.state = cursor.getInt(cursor.getColumnIndex(COLUMN_STATE));
        cursor.close();
        db.close();
        return downloadRequest;
    }

    public int getState(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "select " + COLUMN_STATE + " from " + TABLE + " where " + COLUMN_ID + " = " + id;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor == null) {
            return -1;
        }

        cursor.moveToFirst();
        int state = cursor.getInt(cursor.getColumnIndex(COLUMN_STATE));
        cursor.close();
        db.close();
        return state;
    }

    public void updateState(long id, int state) {

        SQLiteDatabase db = getWritableDatabase();
        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("update ")
                .append(TABLE)
                .append(" set ")
                .append(COLUMN_STATE)
                .append(" = ")
                .append(state)
                .append(" where ")
                .append(COLUMN_ID)
                .append(" = ")
                .append(id);
        db.rawQuery(updateQuery.toString(), null);
        db.close();

    }

    public void updateRequest(DownloadRequest downloadRequest) {
        SQLiteDatabase db = getWritableDatabase();
        String request = new Gson().toJson(downloadRequest);
        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("update ")
            .append(TABLE)
            .append(" set ")
            .append(COLUMN_REQUEST)
            .append(" = " )
            .append(request)
            .append(" , " )
            .append(COLUMN_STATE)
            .append(" = ")
            .append(downloadRequest.state)
            .append(" where ")
            .append(COLUMN_ID)
            .append(" = ")
            .append(downloadRequest.id);
        db.rawQuery(updateQuery.toString(), null);
        db.close();
    }


    public List<DownloadRequest> getAllRequests() {
        List<DownloadRequest> list = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, null, null, null, null, null);
        if(cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                String request = cursor.getString(cursor.getColumnIndex(COLUMN_REQUEST));
                Gson gson = new Gson();
                DownloadRequest downloadRequest = gson.fromJson(request, DownloadRequest.class);
                downloadRequest.state = cursor.getInt(cursor.getColumnIndex(COLUMN_STATE));
                list.add(downloadRequest);
            } while (cursor.moveToNext());
            cursor.close();
            db.close();
        }

        return list;
    }

    public List<DownloadRequest> getAllRequestsOfDownloadState(int state) {
        List<DownloadRequest> list = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, COLUMN_STATE + "=?", new String[]{"" + state}, null, null, null);
        if(cursor != null && cursor.moveToFirst()) {
            list = new ArrayList<>();
            do {
                String request = cursor.getString(cursor.getColumnIndex(COLUMN_REQUEST));
                Gson gson = new Gson();
                DownloadRequest downloadRequest = gson.fromJson(request, DownloadRequest.class);
                downloadRequest.state = cursor.getInt(cursor.getColumnIndex(COLUMN_STATE));
                list.add(downloadRequest);
            } while (cursor.moveToNext());
            cursor.close();
            db.close();
        }

        return list;
    }



}
