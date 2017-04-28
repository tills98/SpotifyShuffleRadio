package de.till_s.spotifyshuffleradio.helper.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Till on 24.04.2017.
 */

public class DbHelperSelect {

    private SQLiteDatabase readableDatabase = null;

    public DbHelperSelect(SQLiteDatabase readableDatabase) {
        this.readableDatabase = readableDatabase;
    }

    public void commit() {

    }

}
