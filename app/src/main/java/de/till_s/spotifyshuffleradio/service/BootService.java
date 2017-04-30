package de.till_s.spotifyshuffleradio.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import de.till_s.spotifyshuffleradio.receiver.MusicIntentReceiver;

public class BootService extends Service {

    private static final String TAG = BootService.class.getSimpleName();

    private static MusicIntentReceiver musicIntentReceiver = null;

    public BootService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Try to register MusicIntentReceiver");

        if (musicIntentReceiver == null && !MusicIntentReceiver.REGISTERD) {
            musicIntentReceiver = new MusicIntentReceiver();

            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(musicIntentReceiver, filter);

            MusicIntentReceiver.REGISTERD = true;

            Log.i(TAG, "MusicIntentReceiver registered");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public static MusicIntentReceiver getMusicIntentReceiver() {
        return musicIntentReceiver;
    }

}
