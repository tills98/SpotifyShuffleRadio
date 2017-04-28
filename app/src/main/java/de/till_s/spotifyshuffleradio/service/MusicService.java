package de.till_s.spotifyshuffleradio.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import de.till_s.spotifyshuffleradio.receiver.MusicIntentReceiver;

/**
 * Created by Till on 27.04.2017.
 */

public class MusicService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);

//        Intent intents = new Intent(getBaseContext(), MainActivity.class);
//        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intents);


        MusicIntentReceiver myReciever = new MusicIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReciever, filter);

        MusicIntentReceiver.REGISTERD = true;

        Log.d("MusicService", "MyService starts MainActivity, register Service Listener");

        return Service.START_STICKY;
    }


}
