package de.till_s.spotifyshuffleradio.helper.spotify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;

import de.till_s.spotifyshuffleradio.helper.tasks.BackgroundTask;
import de.till_s.spotifyshuffleradio.service.OverlayService;

/**
 * Created by Till on 27.04.2017.
 */

public class SpotifyHelper {

    /**
     * Open spotify and start music
     *
     * @param context    Context     Android context
     * @param userUri    String      URI of Spotify user (see Spotify)
     * @param playlistID String      ID of Spotify playlist
     * @param play       boolean     Autoplay
     */
    public static void openSpotify(final Context context, String userUri, String playlistID, boolean play) {
        final String uri = userUri + ":playlist:" + playlistID + (play ? ":play" : "");

        Log.i("SpotifyHelper", uri);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent launcher = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                launcher.setClassName("com.spotify.music", "com.spotify.music.MainActivity");
                launcher.setData(Uri.parse(uri));
                launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launcher);
            }
        };

        BackgroundTask backgroundTask = new BackgroundTask(runnable, null);
        backgroundTask.execute();
    }

    /**
     * Toggle playback state of spotify (not really usable)
     *
     * @param context       Context     Android context
     */
    public static void togglePlay(Context context) {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendOrderedBroadcast(i, null);

        i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendOrderedBroadcast(i, null);
    }

    /**
     * Showing Yes-No Popup
     *
     * @param context       Context     Android context
     */
    public static void showPopup(Context context) {
        Intent intent = new Intent(context, OverlayService.class);
        context.startService(intent);
    }

}
