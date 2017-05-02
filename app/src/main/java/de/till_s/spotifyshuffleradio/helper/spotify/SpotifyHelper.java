package de.till_s.spotifyshuffleradio.helper.spotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.till_s.spotifyshuffleradio.R;
import de.till_s.spotifyshuffleradio.helper.spotify.tasks.SpotifyPlaylistTask;
import de.till_s.spotifyshuffleradio.utils.Settings;
import de.till_s.spotifyshuffleradio.utils.SpotifyAppData;
import de.till_s.spotifyshuffleradio.utils.Utils;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * [Description]
 *
 * @author Till
 * @package de.till_s.spotifyshuffleradio
 * @date 01.05.2017 - 13:57
 */

public class SpotifyHelper implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String TAG = SpotifyHelper.class.getSimpleName();
    private Context context = null;

    private Activity loginActivity = null;

    private boolean spotifyInstalled = false;

    private SpotifyPlayer spotifyPlayer = null;
    private String spotifyClientID = null;
    private String spotifyClientUri = null;
    private String spotifyOAuthToken = null;

    private SpotifyApi spotifyWebApi = null;
    private SpotifyService spotifyWebService = null;

    private Callback<Void> spotifyLoggedInCallback = null;
    private Callback<Void> spotifyOfflineCallback = null;
    private Callback<Void> spotifyLoginFailedCallback = null;

    private Map<String, String> playlistCache = null;

    public SpotifyHelper(Activity loginActivity, Context context) {
        this.loginActivity = loginActivity;
        this.context = context;

        this.spotifyInstalled = Utils.isPackageInstalled("com.spotify.music", context.getPackageManager());

        this.playlistCache = new HashMap<>();
    }

    public boolean isSpotifyInstalled() {
        return spotifyInstalled;
    }

    /**
     * Reload spotify login
     */
    public void reloadLogin() {
        Log.i(TAG, "reloadLogin");
        init();
    }

    /**
     * Load all playlists into a {@see Spinner}
     *
     * @param spinner Spinner which will contains all playlists
     */
    public void reloadPlaylists(Spinner spinner) {
        Log.i(TAG, "reloadPlaylists");
        loadPlaylists(spinner);
    }

    /**
     * Initialize the helper
     */
    public void init() {
        if (Utils.getNetworkConnectivity(context) != Connectivity.OFFLINE) {
            initSpotifyAuth();

            Log.i(TAG, "online");
            spotifyLoggedInCallback.success(null, null);
        } else if (Utils.getNetworkConnectivity(context) == Connectivity.OFFLINE && (Utils.getTimestamp() - Settings.LAST_SPOTIFY_LOGIN) < 24 * 60 * 60) {
            Log.i(TAG, "offline");
            spotifyOfflineCallback.success(null, null);
        } else {
            Log.i(TAG, "failed");
            spotifyLoginFailedCallback.success(null, null);
        }

    }

    /**
     * Destroy the helper
     */
    public void destroy() {

        // Destroy spotify
        Spotify.destroyPlayer(this);

    }

    /**
     * Called when spotify user is logged in
     *
     * @see SpotifyHelper#onLoggedIn()
     */
    private void postInit() {
        if (Utils.getNetworkConnectivity(context) != Connectivity.OFFLINE) {
            initSpotifyWebAPI();

            loadClientUri();
        }
    }

    /**
     * Load spotify client uri
     */
    private void loadClientUri() {
        spotifyWebService.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                Settings.LAST_SPOTIFY_USERURI = spotifyClientUri = userPrivate.uri;
                Settings.saveSettings(context);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    /**
     * Load all playlist into a spinner
     *
     * @param spinner The spinner which will contain all playlists
     */
    public void loadPlaylists(final Spinner spinner) {
        final ArrayAdapter<String> spinnerPlaylistAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        spinnerPlaylistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Callback<Map<String, String>> response = new Callback<Map<String, String>>() {

            @Override
            public void success(Map<String, String> playlists, Response response) {
                playlistCache = playlists;

                int positionSelected = 0;
                Collection<String> playlistNames = playlists.values();

                spinnerPlaylistAdapter.clear();

                if (playlistNames.isEmpty()) {
                    spinnerPlaylistAdapter.add("-- Keine Playlists --");
                } else {
                    spinnerPlaylistAdapter.addAll(playlistNames);
                }

                spinner.setAdapter(spinnerPlaylistAdapter);

                String selectedPlaylistName = playlists.get(Settings.ACTIVE_PLAYLIST);

                if (selectedPlaylistName != null) {
                    positionSelected = spinnerPlaylistAdapter.getPosition(selectedPlaylistName);
                    spinner.setSelection(positionSelected);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }

        };

        loadPlaylists(response, Settings.PLAYLIST_TIMESTAMP == -1 || (Utils.getTimestamp() - Settings.PLAYLIST_TIMESTAMP) > 24 * 60 * 60);
    }

    /**
     * Load all playlists in background with an async task
     *
     * @param response    The callback
     * @param forceReload Force reload all playlists
     * @see SpotifyPlaylistTask
     */
    private void loadPlaylists(Callback<Map<String, String>> response, boolean forceReload) {
        SpotifyPlaylistTask playlistAdapter = new SpotifyPlaylistTask(context, forceReload, spotifyWebService, response);
        playlistAdapter.execute();
    }

    /**
     * Handle the result from spotify login
     *
     * @param requestCode Request code
     * @param resultCode  Result code
     * @param data        The data
     */
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SpotifyAuthHelper.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(context, response.getAccessToken(), SpotifyAppData.CLIENT_ID);

                spotifyClientID = playerConfig.clientId;
                spotifyOAuthToken = playerConfig.oauthToken;

                Settings.LAST_SPOTIFY_USERID = spotifyClientID;
                Settings.saveSettings(context);

                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {

                    @Override
                    public void onInitialized(SpotifyPlayer localSpotifyPlayer) {
                        spotifyPlayer = localSpotifyPlayer;
                        spotifyPlayer.addConnectionStateCallback(SpotifyHelper.this);
                        spotifyPlayer.addNotificationCallback(SpotifyHelper.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                        Toast.makeText(context, context.getString(R.string.spotify_player_error), Toast.LENGTH_SHORT).show();
                    }

                });
            }
        }
    }

    /**
     * Initialize Spotify authentication helper to login an authenticate this wonderful app
     */
    private void initSpotifyAuth() {
        SpotifyAuthHelper spotifyAuthHelper = new SpotifyAuthHelper();
        spotifyAuthHelper.createAuthRequest(loginActivity);
    }

    /**
     * Initialize Web API of Spotify
     */
    private void initSpotifyWebAPI() {
        Log.i(TAG, "initSpotifyWebAPI");

        if (spotifyWebApi == null && spotifyOAuthToken != null) {
            spotifyWebApi = new SpotifyApi();
            spotifyWebApi.setAccessToken(spotifyOAuthToken);

            Log.i(TAG, "Web API set");
        }

        if (spotifyWebApi != null && spotifyWebService == null) {
            spotifyWebService = spotifyWebApi.getService();

            Log.i(TAG, "Web Service set");
        }
    }

    /**
     * Called when user is logged in
     *
     * @param spotifyLoggedInCallback   The callback
     */
    public void setSpotifyLoggedInCallback(Callback<Void> spotifyLoggedInCallback) {
        this.spotifyLoggedInCallback = spotifyLoggedInCallback;
    }

    /**
     * Called when user is offline
     *
     * @param spotifyOfflineCallback    The callback
     */
    public void setSpotifyOfflineCallback(Callback<Void> spotifyOfflineCallback) {
        this.spotifyOfflineCallback = spotifyOfflineCallback;
    }

    /**
     * Called when spotify login failed
     *
     * @param spotifyLoginFailedCallback    The callback
     */
    public void setSpotifyLoginFailedCallback(Callback<Void> spotifyLoginFailedCallback) {
        this.spotifyLoginFailedCallback = spotifyLoginFailedCallback;
    }

    /**
     * @return All cached playlists
     */
    public Map<String, String> getPlaylistCache() {
        return playlistCache;
    }

    /**
     * SPOTIFY SDK
     **/

    @Override
    public void onLoggedIn() {
        Log.i(TAG, "onLoggedIn");

        postInit();
    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {
        spotifyLoginFailedCallback.success(null, null);
    }

    @Override
    public void onTemporaryError() {
        spotifyLoginFailedCallback.success(null, null);
    }

    @Override
    public void onConnectionMessage(String s) {
        spotifyOfflineCallback.success(null, null);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

    }

    @Override
    public void onPlaybackError(Error error) {

    }

}
