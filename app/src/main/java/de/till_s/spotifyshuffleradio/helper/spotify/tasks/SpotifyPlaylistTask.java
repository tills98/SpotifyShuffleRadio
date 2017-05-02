package de.till_s.spotifyshuffleradio.helper.spotify.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.spotify.sdk.android.player.Connectivity;

import java.util.Map;

import de.till_s.spotifyshuffleradio.helper.db.DbHelper;
import de.till_s.spotifyshuffleradio.helper.db.DbHelperInsert;
import de.till_s.spotifyshuffleradio.helper.db.DbUtils;
import de.till_s.spotifyshuffleradio.helper.spotify.db.SpotifyPlaylistContract;
import de.till_s.spotifyshuffleradio.utils.Settings;
import de.till_s.spotifyshuffleradio.utils.Utils;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * [Description]
 *
 * @author Till
 * @package de.till_s.spotifyshuffleradio
 * @date 01.05.2017 - 14:49
 */

public class SpotifyPlaylistTask extends AsyncTask<Void, Void, Map<String, String>> {

    private static final String TAG = SpotifyPlaylistTask.class.getSimpleName();

    private Context context;

    private SpotifyService spotifyService;

    private boolean forceReload = false;

    private Callback<Map<String, String>> response;

    /**
     * Task for loading all user playlists from web API
     *
     * @param context        Android context
     * @param forceReload    Force reload all playlists
     * @param spotifyService The spotify web service from Spotify Web-API
     * @param response       Callback
     */
    public SpotifyPlaylistTask(Context context, boolean forceReload, SpotifyService spotifyService, Callback<Map<String, String>> response) {
        this.context = context;
        this.forceReload = forceReload;
        this.spotifyService = spotifyService;
        this.response = response;
    }

    private Map<String, String> getLocalPlaylists() {
        return DbUtils.getPlaylists(context);
    }

    /**
     * Refresh local database when forceReaload is set
     *
     * @param params ... Nothing
     * @return Returns local saved playlists from database
     */
    @Override
    protected Map<String, String> doInBackground(Void... params) {
        Settings.loadSettings(context);

        if (forceReload && Utils.getNetworkConnectivity(context) != Connectivity.OFFLINE) {
            getApiPlaylists();
        }

        return getLocalPlaylists();
    }

    /**
     * The result
     *
     * @param stringStringMap Map
     */
    @Override
    protected void onPostExecute(Map<String, String> stringStringMap) {
        super.onPostExecute(stringStringMap);

        response.success(stringStringMap, null);
    }

    /**
     * Get spotify playlists from web api
     */
    private void getApiPlaylists() {
        Pager<PlaylistSimple> playlistSimplePager = null;

        if (spotifyService != null) {
            try {
                playlistSimplePager = spotifyService.getMyPlaylists();
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);

                Log.e(TAG, "Error: " + spotifyError.toString());
            }
        } else {
            Log.e(TAG, "Error ... spotify service null");
        }


        if (playlistSimplePager != null) {
            DbUtils.deleteAllPlaylists(context);

            Log.i(TAG, "Count pager playlists: " + playlistSimplePager.items);

            // Store playlists local
            for (PlaylistSimple playlist : playlistSimplePager.items) {
                String playlistID = playlist.id;
                String playlistTitle = playlist.name;

                ContentValues dbPlaylist = new ContentValues();

                // Create new playlist entry in database
                dbPlaylist.put(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_PLAYLIST_ID, playlistID);
                dbPlaylist.put(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_NAME_TITLE, playlistTitle);

                createPlaylistEntry(dbPlaylist);
            }

            // Update playlist timestamp in settings
            Settings.PLAYLIST_TIMESTAMP = Utils.getTimestamp();
            Settings.saveSettings(context);
        } else {
            Log.e(TAG, "Error ... playlist pager null");
        }
    }

    /**
     * Store a single playlist entry to local database
     *
     * @param values
     * @return
     */
    private void createPlaylistEntry(ContentValues values) {
        DbHelper.getInstance().createDatabaseHandler(context);
        DbHelperInsert insert = DbHelper.getInstance().insert();

        Log.i(TAG, "Inserted: " + values.valueSet().size());

        insert.setContentValues(values);
        insert.commit();

    }
}
