package de.till_s.spotifyshuffleradio;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Till on 24.04.2017.
 */

public final class Settings {

    /**
     *
     */
    public static boolean APP_ACTIVE = false;

    /**
     *
     */
    public static boolean ASK_EVERYTIME = true;

    /**
     *
     */
    public static String ACTIVE_PLAYLIST = null;

    /**
     *
     */
    public static int PLAYLIST_TIMESTAMP = -1;

    /**
     *
     */
    public static int LAST_SPOTIFY_LOGIN = -1;

    /**
     *
     */
    public static String LAST_SPOTIFY_USERID = null;

    /**
     *
     */
    public static String LAST_SPOTIFY_USERNAME = null;

    /**
     *
     * @param context
     */
    public static void loadSettings(Context context) {
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.preferences_settings), Context.MODE_PRIVATE);

        APP_ACTIVE = pref.getBoolean(context.getString(R.string.preferences_app_state), false);
        ASK_EVERYTIME = pref.getBoolean(context.getString(R.string.preferences_ask_everytime), true);
        ACTIVE_PLAYLIST = pref.getString(context.getString(R.string.preferences_active_playlist), null);
        PLAYLIST_TIMESTAMP = pref.getInt(context.getString(R.string.preferences_playlist_timestamp), -1);
        LAST_SPOTIFY_LOGIN = pref.getInt(context.getString(R.string.preferences_last_spotify_login), -1);
        LAST_SPOTIFY_USERID = pref.getString(context.getString(R.string.preferences_last_spotify_userid), null);
        LAST_SPOTIFY_USERNAME = pref.getString(context.getString(R.string.preferences_last_spotify_username), null);
    }

    /**
     *
     * @param context
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
        editor.putString(context.getString(R.string.preferences_last_spotify_username), LAST_SPOTIFY_USERNAME);

        editor.apply();
    }

    public static void resetSettings(Context context) {
        APP_ACTIVE = false;
        ASK_EVERYTIME = true;
        ACTIVE_PLAYLIST = null;
        PLAYLIST_TIMESTAMP = -1;
        LAST_SPOTIFY_LOGIN = -1;
        LAST_SPOTIFY_USERID = null;
        LAST_SPOTIFY_USERNAME = null;

        saveSettings(context);
    }

}
