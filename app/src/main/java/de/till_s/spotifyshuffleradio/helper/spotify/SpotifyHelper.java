package de.till_s.spotifyshuffleradio.helper.spotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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
import de.till_s.spotifyshuffleradio.Settings;
import de.till_s.spotifyshuffleradio.helper.spotify.tasks.SpotifyPlaylistTask;
import de.till_s.spotifyshuffleradio.utils.SpotifyAppData;
import de.till_s.spotifyshuffleradio.utils.Utils;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
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
    private final TextView spotifyStatusTextView;

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

    public SpotifyHelper(Activity loginActivity, Context context, TextView spotifyStatusTextView) {
        this.loginActivity = loginActivity;
        this.context = context;

        this.spotifyInstalled = Utils.isPackageInstalled("com.spotify.music", context.getPackageManager());

        this.spotifyStatusTextView = spotifyStatusTextView;

        this.playlistCache = new HashMap<>();
    }

    public boolean isSpotifyInstalled() {
        return spotifyInstalled;
    }

    public void reloadLogin() {
        Log.i(TAG, "reloadLogin");
        init();
    }

    public void reloadPlaylists(Spinner spinner) {
        Log.i(TAG, "reloadPlaylists");
        loadPlaylists(spinner, true);
    }

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

    public void destroy() {

        // Destroy spotify
        Spotify.destroyPlayer(this);

    }

    private void postInit() {
        if (Utils.getNetworkConnectivity(context) != Connectivity.OFFLINE) {
            initSpotifyWebAPI();
        }
    }

    public void loadPlaylists(final Spinner spinner, boolean forceReload) {
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

        loadPlaylists(response, forceReload);
    }

    public void loadPlaylists(Callback<Map<String, String>> response, boolean forceReload) {
        SpotifyPlaylistTask playlistAdapter = new SpotifyPlaylistTask(context, forceReload, spotifyWebService, response);
        playlistAdapter.execute();
    }

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

    private void initSpotifyAuth() {
        SpotifyAuthHelper spotifyAuthHelper = new SpotifyAuthHelper();
        spotifyAuthHelper.createAuthRequest(loginActivity);
    }

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


    public void setSpotifyLoggedInCallback(Callback<Void> spotifyLoggedInCallback) {
        this.spotifyLoggedInCallback = spotifyLoggedInCallback;
    }

    public void setSpotifyOfflineCallback(Callback<Void> spotifyOfflineCallback) {
        this.spotifyOfflineCallback = spotifyOfflineCallback;
    }

    public void setSpotifyLoginFailedCallback(Callback<Void> spotifyLoginFailedCallback) {
        this.spotifyLoginFailedCallback = spotifyLoginFailedCallback;
    }

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
