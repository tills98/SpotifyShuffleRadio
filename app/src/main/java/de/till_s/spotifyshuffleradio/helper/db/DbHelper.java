package de.till_s.spotifyshuffleradio.helper.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyPlaylistContract;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyPlaylistDbHelper;

/**
 * Created by Till on 24.04.2017.
 */

public class DbHelper {

    private static final DbHelper ourInstance = new DbHelper();

    public static DbHelper getInstance() {
        return ourInstance;
    }

    private SpotifyPlaylistDbHelper databaseHelper = null;
    private SQLiteDatabase readableDatabase = null;
    private SQLiteDatabase writableDatabase = null;

    private DbHelper() {}

    public DbHelperInsert insert() {
        if (writableDatabase == null) createWritableDatabase();
        return new DbHelperInsert(writableDatabase);
    }

    public Cursor select(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return select(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    public Cursor select(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        if (readableDatabase == null) createReadableDatabase();

        return readableDatabase.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public int deleteAll() {
        if (writableDatabase == null) createWritableDatabase();

        return writableDatabase.delete(SpotifyPlaylistContract.SpoitfyPlaylistEntry.TABLE_NAME, null, null);
    }

    public void createDatabaseHandler(Context context) {
        databaseHelper = new SpotifyPlaylistDbHelper(context);
    }

    private void createReadableDatabase() {
        if (databaseHelper != null) {
            readableDatabase = databaseHelper.getReadableDatabase();
        }
    }

    private void createWritableDatabase() {
        if (databaseHelper != null) {
            writableDatabase = databaseHelper.getWritableDatabase();
        }
    }

    public SQLiteDatabase getReadableDatabase() {
        if (readableDatabase == null) createReadableDatabase();
        return readableDatabase;
    }

    public SQLiteDatabase getWritableDatabase() {
        if (writableDatabase == null) createWritableDatabase();
        return writableDatabase;
    }

    public void destroy() {
        destroyReadableDatabase();
        destroyWritableDatabase();
        destroyDatabaseHelper();
    }

    private void destroyReadableDatabase() {
        if (readableDatabase != null) {
            readableDatabase.close();
            readableDatabase = null;
        }
    }

    private void destroyWritableDatabase() {
        if (writableDatabase != null) {
            writableDatabase.close();
            writableDatabase = null;
        }
    }

    private void destroyDatabaseHelper() {
        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
    }



}
