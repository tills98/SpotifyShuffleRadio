package de.till_s.spotifyshuffleradio;

import android.content.Context;
import android.content.SharedPreferences;

import de.till_s.spotifyshuffleradio.receiver.HeadsetReceiver;

/**
 * Settings storage from key-value preferences
 *
 * @author Till S.
 */
public final class Settings {

    /**
     * App will listening for {@see Intent#ACTION_HEADSET_PLUG}
     * @see HeadsetReceiver
     */
    public static boolean APP_ACTIVE = false;

    /**
     * App will ask you for every headset plug
     * @see HeadsetReceiver
     */
    public static boolean ASK_EVERYTIME = true;

    /**
     * Storing active playlist which will be played if headset is plugged
     */
    public static String ACTIVE_PLAYLIST = null;

    /**
     * Last update timestamp of spotify playlist list
     */
    public static int PLAYLIST_TIMESTAMP = -1;

    /**
     * Last spotify login timestamp (used for offline usage)
     */
    public static int LAST_SPOTIFY_LOGIN = -1;

    /**
     * Last spotify userID
     */
    public static String LAST_SPOTIFY_USERID = null;

    /**
     * Last spotify URI
     */
    public static String LAST_SPOTIFY_USERURI = null;

    /**
     * Load settings from key-value preference
     *
     * @param context       Context     Android context
     */
    public static void loadSettings(Context context) {
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.preferences_settings), Context.MODE_PRIVATE);

        APP_ACTIVE = pref.getBoolean(context.getString(R.string.preferences_app_state), false);
        ASK_EVERYTIME = pref.getBoolean(context.getString(R.string.preferences_ask_everytime), true);
        ACTIVE_PLAYLIST = pref.getString(context.getString(R.string.preferences_active_playlist), null);
        PLAYLIST_TIMESTAMP = pref.getInt(context.getString(R.string.preferences_playlist_timestamp), -1);
        LAST_SPOTIFY_LOGIN = pref.getInt(context.getString(R.string.preferences_last_spotify_login), -1);
        LAST_SPOTIFY_USERID = pref.getString(context.getString(R.string.preferences_last_spotify_userid), null);
        LAST_SPOTIFY_USERURI = pref.getString(context.getString(R.string.preferences_last_spotify_useruri), null);
    }

    /**
     * Save all settings to preference
     *
     * @param context       Context     Android context
     */
    public static void saveSettings(Context context) {
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.preferences_settings), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(context.getString(R.string.preferences_app_state), APP_ACTIVE);
        editor.putBoolean(context.getString(R.string.preferences_ask_everytime), ASK_EVERYTIME);
        editor.putString(context.getString(R.string.preferences_active_playlist), ACTIVE_PLAYLIST);
        editor.putInt(context.getString(R.string.preferences_playlist_timestamp), PLAYLIST_TIMESTAMP);
        editor.putInt(context.getString(R.string.preferences_last_spotify_login), LAST_SPOTIFY_LOGIN);
        editor.putString(context.getString(R.string.preferences_last_spotify_userid), LAST_SPOTIFY_USERID);
        editor.putString(context.getString(R.string.preferences_last_spotify_useruri), LAST_SPOTIFY_USERURI);

        editor.apply();
    }

    /**
     * Reset all settings (used in {@see de.till_s.spotifyshuffleradio.DatabaseTest}
     *
     * @param context Context     Android context
     */
    public static void resetSettings(Context context) {
        APP_ACTIVE = false;
        ASK_EVERYTIME = true;
        ACTIVE_PLAYLIST = null;
        PLAYLIST_TIMESTAMP = -1;
        LAST_SPOTIFY_LOGIN = -1;
        LAST_SPOTIFY_USERID = null;
        LAST_SPOTIFY_USERURI = null;

        saveSettings(context);
    }

}
