package de.till_s.spotifyshuffleradio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyHelper;

public class NotificationResultService extends Service {

    public NotificationResultService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String result = null;

        if (intent != null && intent.getExtras().size() == 1) {
            result = intent.getStringExtra(SpotifyHelper.NOTIFY_RESULT_KEY);
        }


        switch (result) {

            case SpotifyHelper.NOTIFY_RESULT_ACCEPT:

                // start spotify
                SpotifyHelper.openSpotify(this, Settings.LAST_SPOTIFY_USERNAME, Settings.ACTIVE_PLAYLIST, true);

                // close notification
                SpotifyHelper.closeNotification(this);

                break;

            case SpotifyHelper.NOTIFY_RESULT_DECLINE:
            default:

                // close notification only
                SpotifyHelper.closeNotification(this);

                break;

        }

        return super.onStartCommand(intent, flags, startId);
    }


}
