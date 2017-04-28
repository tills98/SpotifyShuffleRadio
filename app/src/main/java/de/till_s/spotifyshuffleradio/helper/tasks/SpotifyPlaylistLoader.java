package de.till_s.spotifyshuffleradio.helper.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.till_s.spotifyshuffleradio.helper.db.DbHelper;
import de.till_s.spotifyshuffleradio.helper.db.DbUtils;
import retrofit.Callback;

/**
 * Created by Till on 24.04.2017.
 */

public class SpotifyPlaylistLoader extends AsyncTask<Object, Void, Map<String, String>> {

    private Context context;
    private Callback<Map<String, String>> response;

    /**
     * Initialize the task
     *
     * @param context       Context
     * @param response      Callback
     */
    public SpotifyPlaylistLoader(Context context, Callback<Map<String, String>> response) {
        this.context = context;
        this.response = response;
    }

    /**
     * The runner
     *
     * @param params    Object[]
     * @return          Map
     */
    @Override
    protected Map<String, String> doInBackground(Object... params) {
        return DbUtils.getPlaylists(context);
    }

    /**
     * The result
     *
     * @param stringStringMap   Map
     */
    @Override
    protected void onPostExecute(Map<String, String> stringStringMap) {
        super.onPostExecute(stringStringMap);

        response.success(stringStringMap, null);
    }

}
