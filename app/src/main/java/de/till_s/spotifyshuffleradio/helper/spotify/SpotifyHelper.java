package de.till_s.spotifyshuffleradio.helper.spotify;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;

import de.till_s.spotifyshuffleradio.NotificationResultService;
import de.till_s.spotifyshuffleradio.R;

/**
 * Created by Till on 27.04.2017.
 */

public class SpotifyHelper {

    public static final String NOTIFY_RESULT_KEY = "de.till_s.spotfiyshuffleradio.NOTIFY_RESULT_KEY";
    public static final String NOTIFY_RESULT_ACCEPT = "ACCEPT";
    public static final String NOTIFY_RESULT_DECLINE = "DECLINE";

    public static final String NOTIFY_ID_KEY = "de.till_s.spotfiyshuffleradio.NOTIFY_ID_KEY";
    private static final int NOTIFY_ID = 99291543;

    public static void openSpotify(Context context, String userUri, String playlistID, boolean play) {
        String uri = userUri + ":playlist:" + playlistID + (play ? ":play" : "");

        Log.i("SpotifyHelper", uri);

        Intent launcher = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        launcher.setClassName("com.spotify.music", "com.spotify.music.MainActivity");
        launcher.setData(Uri.parse(uri));
        launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launcher);
    }

    public static void togglePlay(Context context) {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendOrderedBroadcast(i, null);

        i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendOrderedBroadcast(i, null);
    }

    public static void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(context.getString(R.string.ask_title))
                .setContentText(context.getString(R.string.ask_description));

        // Accept intent
        Intent acceptIntent = new Intent(context, NotificationResultService.class);
        acceptIntent.putExtra(NOTIFY_RESULT_KEY, NOTIFY_RESULT_ACCEPT);
        PendingIntent acceptPendingIntent = PendingIntent.getService(context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // add actions to notification
        notificationBuilder.setContentIntent(acceptPendingIntent);
        notificationBuilder.setFullScreenIntent(acceptPendingIntent, true);

        notificationBuilder.addAction(android.R.drawable.ic_media_play, context.getString(R.string.ask_yes_button), acceptPendingIntent);

        // notify user
        notificationManager.notify(NOTIFY_ID, notificationBuilder.build());
    }

    public static void closeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFY_ID);
    }

}
