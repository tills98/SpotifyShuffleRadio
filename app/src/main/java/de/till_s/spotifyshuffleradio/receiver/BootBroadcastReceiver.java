package de.till_s.spotifyshuffleradio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.till_s.spotifyshuffleradio.MainActivity;

/**
 * Created by Till on 27.04.2017.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("BootBroadcastReceiver", "Start background service");

        context.startService(new Intent(context, MainActivity.class));
    }

}
