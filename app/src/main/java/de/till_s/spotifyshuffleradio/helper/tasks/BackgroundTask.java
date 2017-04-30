package de.till_s.spotifyshuffleradio.helper.tasks;

import android.os.AsyncTask;

import retrofit.Callback;

/**
 * Created by Till on 27.04.2017.
 */

public class BackgroundTask extends AsyncTask<Void, Void, Void> {

    private Runnable runnable;
    private Callback<Void> response;

    /**
     * Initialize the task
     *
     * @param response      Callback
     */
    public BackgroundTask(Runnable runnable, Callback<Void> response) {
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
        if (runnable != null) runnable.run();

        return null;
    }

    /**
     * Execute response
     * @param v
     */
    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);

        if (response != null) response.success(v, null);
    }

}
