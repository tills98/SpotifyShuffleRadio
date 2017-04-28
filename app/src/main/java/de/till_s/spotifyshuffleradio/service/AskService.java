package de.till_s.spotifyshuffleradio.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import de.till_s.spotifyshuffleradio.R;
import de.till_s.spotifyshuffleradio.Settings;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyHelper;

public class AskService extends Service {

    private static final String TAG = AskService.class.getSimpleName();
    public static final String POPUP_MESSAGE = "de.till_s.spotifyshuffleradio.POPUP_MESSAGE";

    private WindowManager windowManager;
    private View view;

    public AskService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void showDialog() {
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock((WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | PowerManager.ACQUIRE_CAUSES_WAKEUP), "AskService");
        wakeLock.acquire();
        wakeLock.release();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        view = View.inflate(getApplicationContext(), R.layout.activity_ask_popup, null);
        view.setTag(TAG);

        int top = getApplicationContext().getResources().getDisplayMetrics().heightPixels / 2;

        LinearLayout dialog = (LinearLayout) view.findViewById(R.id.pop_exit);

        Button yesButton = (Button) view.findViewById(R.id.buttonYes);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.loadSettings(getApplicationContext());

                if (Settings.LAST_SPOTIFY_USERNAME != null && Settings.ACTIVE_PLAYLIST != null) {
                    SpotifyHelper.openSpotify(getApplicationContext(), Settings.LAST_SPOTIFY_USERNAME, Settings.ACTIVE_PLAYLIST, true);
                }

                Log.i(TAG, "Start Spotify");
            }
        });

        Button noButton = (Button) view.findViewById(R.id.buttonNo);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDialog();

                Log.i(TAG, "Hide dialog");
            }
        });

        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON ,
                PixelFormat.RGBA_8888
        );

        view.setVisibility(View.VISIBLE);
        windowManager.addView(view, layoutParams);
        windowManager.updateViewLayout(view, layoutParams);
    }

    private void hideDialog() {
        if (windowManager != null && view != null) {
            windowManager.removeView(view);
            view = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
