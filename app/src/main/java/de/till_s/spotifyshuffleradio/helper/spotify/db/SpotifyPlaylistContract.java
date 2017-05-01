package de.till_s.spotifyshuffleradio.helper.spotify.db;

import android.provider.BaseColumns;

/**
 * Created by Till on 24.04.2017.
 */

public final class SpotifyPlaylistContract {

    private SpotifyPlaylistContract() {

    }

    public static class SpoitfyPlaylistEntry implements BaseColumns {

        public static final String TABLE_NAME = "spotify_playlists";

        public static final String COLUMN_PLAYLIST_ID = "playlistID";
        public static final String COLUMN_NAME_TITLE = "title";

    }

}
