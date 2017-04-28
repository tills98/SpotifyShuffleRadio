package de.till_s.spotifyshuffleradio.helper.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyPlaylistContract;

/**
 * Created by Till on 24.04.2017.
 */

public class DbHelperInsert {

    private SQLiteDatabase writableDatabase = null;
    private ContentValues values;

    public DbHelperInsert(SQLiteDatabase writableDatabase) {
        this.writableDatabase = writableDatabase;
    }

    public void addString(String column, String value) {
        if (values == null) {
            values = new ContentValues();
        }

        values.put(column, value);
    }

    public void setContentValues(ContentValues values) {
        this.values = values;
    }

    public long commit() {
        return writableDatabase.insert(SpotifyPlaylistContract.SpoitfyPlaylistEntry.TABLE_NAME, null, values);
    }

}
