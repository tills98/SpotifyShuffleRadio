package de.till_s.spotifyshuffleradio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import de.till_s.spotifyshuffleradio.Settings;
import de.till_s.spotifyshuffleradio.helper.spotify.utils.SpotifyUtils;

/**
 * Created by Till on 27.04.2017.
 */

public class HeadsetReceiver extends BroadcastReceiver {

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
                        Log.i("HeadsetReceiver", "Aux detected");

                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                        if (Settings.LAST_SPOTIFY_USERURI != null && Settings.ACTIVE_PLAYLIST != null && audioManager.isWiredHeadsetOn()) {

                            if (Settings.ASK_EVERYTIME) {
//                                SpotifyUtils.showNotification(context);
                                SpotifyUtils.showPopup(context);
                            } else {
                                SpotifyUtils.openSpotify(context, Settings.LAST_SPOTIFY_USERURI, Settings.ACTIVE_PLAYLIST, true);
                            }

                        }

                        break;

                    // Headset unplugged
                    case 0:
                        Log.i("HeadsetReceiver", "Aux unplugged");
                        // TODO: Check playback state
                        // com.spotify.music.playbackstatechanged

                        //SpotifyUtils.togglePlay(context);

                        break;
                    default:

                }
            }

        }
    }

}
