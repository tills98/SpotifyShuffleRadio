package de.till_s.spotifyshuffleradio.helper.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.till_s.spotifyshuffleradio.helper.spotify.db.SpotifyPlaylistContract;
import de.till_s.spotifyshuffleradio.helper.spotify.db.SpotifyPlaylistDbHelper;

/**
 * Created by Till on 24.04.2017.
 */

public class DbHelper {

    private static final DbHelper ourInstance = new DbHelper();

    /**
     * The instance
     *
     * @return The instance
     */
    public static DbHelper getInstance() {
        return ourInstance;
    }

    private SpotifyPlaylistDbHelper databaseHelper = null;
    private SQLiteDatabase readableDatabase = null;
    private SQLiteDatabase writableDatabase = null;

    private DbHelper() {}

    /**
     * An helper for inserts
     *
     * @return An selfmade insert helper
     */
    public DbHelperInsert insert() {
        if (writableDatabase == null) createWritableDatabase();
        return new DbHelperInsert(writableDatabase);
    }

    /**
     * Select something from database
     *
     * @param table         The table
     * @param columns       List of columns
     * @param selection     Where clause
     * @param selectionArgs Where clause values
     * @param groupBy       GROUP BY syntax
     * @param having        HAVING syntax
     * @param orderBy       ORDER BY syntax
     * @return The {@see Cursor} object from Android SDK
     */
    public Cursor select(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return select(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    /**
     * Select something from database
     *
     * @param table         The table
     * @param columns       List of columns
     * @param selection     Where clause
     * @param selectionArgs Where clause values
     * @param groupBy       GROUP BY syntax
     * @param having        HAVING syntax
     * @param orderBy       ORDER BY syntax
     * @param limit         LIMIT syntax
     * @return The {@see Cursor} object from Android SDK
     */
    public Cursor select(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        if (readableDatabase == null) createReadableDatabase();

        return readableDatabase.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * Delete all values from table
     *
     * @return Amount of rows which are deleted
     */
    public int deleteAll() {
        if (writableDatabase == null) createWritableDatabase();

        return writableDatabase.delete(SpotifyPlaylistContract.SpoitfyPlaylistEntry.TABLE_NAME, null, null);
    }

    /**
     * Open database handler
     *
     * @param context   Android context
     */
    public void createDatabaseHandler(Context context) {
        databaseHelper = new SpotifyPlaylistDbHelper(context);
    }

    /**
     * Create the {@see SQLiteDatabase} readable object if
     */
    private void createReadableDatabase() {
        if (databaseHelper != null) {
            readableDatabase = databaseHelper.getReadableDatabase();
        }
    }

    /**
     * Create the {@see SQLiteDatabase} writable object if
     */
    private void createWritableDatabase() {
        if (databaseHelper != null) {
            writableDatabase = databaseHelper.getWritableDatabase();
        }
    }

    /**
     * @return The {@see SQLiteDatabase} readable database object
     */
    public SQLiteDatabase getReadableDatabase() {
        if (readableDatabase == null) createReadableDatabase();
        return readableDatabase;
    }

    /**
     * @return The {@see SQLiteDatabase} writable database object
     */
    public SQLiteDatabase getWritableDatabase() {
        if (writableDatabase == null) createWritableDatabase();
        return writableDatabase;
    }

    /**
     * Destroy all objects. Called in {@see MainActivity@onDestroy}
     */
    public void destroy() {
        destroyReadableDatabase();
        destroyWritableDatabase();
        destroyDatabaseHelper();
    }

    /**
     * Destroy the {@see SQLiteDatabase} readable database
     */
    private void destroyReadableDatabase() {
        if (readableDatabase != null) {
            readableDatabase.close();
            readableDatabase = null;
        }
    }

    /**
     * Destroy the {@see SQLiteDatabase} writable database
     */
    private void destroyWritableDatabase() {
        if (writableDatabase != null) {
            writableDatabase.close();
            writableDatabase = null;
        }
    }

    /**
     * Destroy the database helper
     */
    private void destroyDatabaseHelper() {
        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
    }



}
