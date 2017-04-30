package de.till_s.spotifyshuffleradio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.till_s.spotifyshuffleradio.service.BootService;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = BootService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Boot receiver");

        context.startService(new Intent(context, BootService.class));
    }
}
