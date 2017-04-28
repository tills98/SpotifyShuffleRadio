package de.till_s.spotifyshuffleradio.helper.spotify;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Till on 24.04.2017.
 */

public class SpotifyPlaylistDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME  = "SpotifyPlaylists.db";
    public static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SpotifyPlaylistContract.SpoitfyPlaylistEntry.TABLE_NAME + " ( " +
                    SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_PLAYLIST_ID +" TEXT PRIMARY KEY, " +
                    SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_NAME_TITLE +" TEXT " +
            ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SpotifyPlaylistContract.SpoitfyPlaylistEntry.TABLE_NAME;

    public SpotifyPlaylistDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.i("SpotifyPlaylistDbHelper", "Table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
