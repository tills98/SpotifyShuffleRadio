package de.till_s.spotifyshuffleradio.helper.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.till_s.spotifyshuffleradio.helper.spotify.db.SpotifyPlaylistContract;

/**
 * Created by Till on 24.04.2017.
 */

public final class DbUtils {

    /**
     * Get all stored playlists
     * Attention: This database query working synchronous! For async database query
     * @see DbAsyncUtils@getPlaylists
     *
     * @param context   Context
     * @return          Map
     */
    public static Map<String, String> getPlaylists(Context context) {
        DbHelper.getInstance().createDatabaseHandler(context);

        String[] projection = {
                SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_PLAYLIST_ID,
                SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_NAME_TITLE
        };

        Cursor cursor = DbHelper.getInstance().select(
                SpotifyPlaylistContract.SpoitfyPlaylistEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        Map<String, String> items = new HashMap<>();
        while (cursor.moveToNext()) {
            String playlistID = cursor.getString(cursor.getColumnIndexOrThrow(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_PLAYLIST_ID));
            String playlistName = cursor.getString(cursor.getColumnIndexOrThrow(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_NAME_TITLE));

            items.put(playlistID, playlistName);
            Log.i("DbUtils", "Add playlist");
        }

        Log.i("DbUtils", "Playlists: " + items.size());
        return items;
    }

    /**
     * Delete all playlists
     *
     * @param context Android context
     * @return Amount of deleted playlists
     */
    public static int deleteAllPlaylists(Context context) {
        DbHelper.getInstance().createDatabaseHandler(context);

        return DbHelper.getInstance().deleteAll();
    }

}
