package de.till_s.spotifyshuffleradio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyPlaylistContract;
import de.till_s.spotifyshuffleradio.helper.db.DbHelper;
import de.till_s.spotifyshuffleradio.helper.db.DbUtils;

import static org.junit.Assert.assertEquals;

/**
 * Created by Till on 24.04.2017.
 */

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    @Test
    public void testInsert() throws Exception {
        // Context of the app under test.
        Context context = InstrumentationRegistry.getTargetContext();

        ContentValues values = new ContentValues();

        values.put(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_PLAYLIST_ID, "37i9dQZF1DX4jP4eebSWR9");
        values.put(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_NAME_TITLE, "Top Hits Deutschland");


        DbHelper.getInstance().createDatabaseHandler(context);

//        DbHelper.getInstance().getWritableDatabase().deleteAll(SpotifyPlaylistContract.SpoitfyPlaylistEntry.TABLE_NAME, SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_PLAYLIST_ID + " = " + "37i9dQZF1DX4jP4eebSWR9", null);
        Cursor cursor = DbHelper.getInstance().getReadableDatabase().rawQuery("SELECT * FROM spotify_playlists", null);

        while (cursor.moveToNext()) {
            android.util.Log.d("DatabaseTest", cursor.getString(cursor.getColumnIndexOrThrow(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_PLAYLIST_ID)));
        }

        cursor.close();
    }

    @Test
    public void testUpdate() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        DbHelper.getInstance().createDatabaseHandler(context);
        DbHelper.getInstance().deleteAll();

        Map<String, String> playlists = DbUtils.getPlaylists(context);
        assertEquals(playlists.size(), 0);
    }

    @Test
    public void resetSettings() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();

        Settings.resetSettings(context);

        assertEquals(Settings.APP_ACTIVE, false);
        assertEquals(Settings.ACTIVE_PLAYLIST, null);
        assertEquals(Settings.PLAYLIST_TIMESTAMP, -1);
        assertEquals(Settings.LAST_SPOTIFY_LOGIN, -1);
        assertEquals(Settings.LAST_SPOTIFY_USERID, null);
    }
}
