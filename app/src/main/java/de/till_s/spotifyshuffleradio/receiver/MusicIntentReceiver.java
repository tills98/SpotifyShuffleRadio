package de.till_s.spotifyshuffleradio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.till_s.spotifyshuffleradio.Settings;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyHelper;
import de.till_s.spotifyshuffleradio.service.AskService;

/**
 * Created by Till on 27.04.2017.
 */

public class MusicIntentReceiver extends BroadcastReceiver {

    public static boolean REGISTERD = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            Settings.loadSettings(context);

            if (Settings.APP_ACTIVE) {
                int state = intent.getIntExtra("state", -1);

                switch (state) {
                    // Headset plugged
                    case 1:
                        Log.i("MusicIntentReceiver", "Aux detected");

                        if (Settings.LAST_SPOTIFY_USERNAME != null && Settings.ACTIVE_PLAYLIST != null) {
                            SpotifyHelper.showNotification(context);
                        }

                        break;

                    // Headset unplugged
                    case 0:
                        Log.i("MusicIntentReceiver", "Aux unplugged");
                        // TODO: Check playback state
                        // com.spotify.music.playbackstatechanged

                        //SpotifyHelper.togglePlay(context);

                        break;
                    default:

                }
            }

        }
    }

}
