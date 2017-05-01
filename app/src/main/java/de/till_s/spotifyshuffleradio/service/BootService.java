package de.till_s.spotifyshuffleradio.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import de.till_s.spotifyshuffleradio.receiver.HeadsetReceiver;

public class BootService extends Service {

    private static final String TAG = BootService.class.getSimpleName();

    private static HeadsetReceiver headsetReceiver = null;

    public BootService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Try to register HeadsetReceiver");

        if (headsetReceiver == null && !HeadsetReceiver.REGISTERD) {
            headsetReceiver = new HeadsetReceiver();

            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(headsetReceiver, filter);

            HeadsetReceiver.REGISTERD = true;

            Log.i(TAG, "HeadsetReceiver registered");
        }

        return Service.START_STICKY;
    }

    public static HeadsetReceiver getHeadsetReceiver() {
        return headsetReceiver;
    }

}
