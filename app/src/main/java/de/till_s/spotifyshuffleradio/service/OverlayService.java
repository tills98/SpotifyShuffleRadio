package de.till_s.spotifyshuffleradio.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import de.till_s.spotifyshuffleradio.activities.OverlayActivity;

public class OverlayService extends Service {

    public OverlayService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE,
                "OverlayService");

        wakeLock.acquire();
        wakeLock.release();*/

        showOverlayActivity(this);

        return super.onStartCommand(intent, flags, startId);
    }

    private void showOverlayActivity(Context context) {
        Intent intent = new Intent(context, OverlayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
