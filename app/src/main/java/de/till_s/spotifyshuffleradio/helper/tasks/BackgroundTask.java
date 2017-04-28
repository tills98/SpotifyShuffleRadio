package de.till_s.spotifyshuffleradio.helper.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.concurrent.Executor;

import retrofit.Callback;

/**
 * Created by Till on 27.04.2017.
 */

public class BackgroundTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private Runnable runnable;
    private Callback<Void> response;

    /**
     * Initialize the task
     *
     * @param context       Context
     * @param response      Callback
     */
    public BackgroundTask(Context context, Runnable runnable, Callback<Void> response) {
        this.context = context;
        this.runnable = runnable;
        this.response = response;
    }

    /**
     *
     *
     * @param params
     * @return
     */
    @Override
    protected Void doInBackground(Void... params) {
        runnable.run();

        return null;
    }

    /**
     * Execute response
     * @param v
     */
    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

        response.success(v, null);
    }

}
