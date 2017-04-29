package de.till_s.spotifyshuffleradio;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.till_s.spotifyshuffleradio.helper.db.DbHelper;
import de.till_s.spotifyshuffleradio.helper.db.DbHelperInsert;
import de.till_s.spotifyshuffleradio.helper.db.DbUtils;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyAuthHelper;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyHelper;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyPlaylistContract;
import de.till_s.spotifyshuffleradio.helper.tasks.SpotifyPlaylistLoader;
import de.till_s.spotifyshuffleradio.receiver.MusicIntentReceiver;
import de.till_s.spotifyshuffleradio.utils.SpotifyAppData;
import de.till_s.spotifyshuffleradio.utils.Utils;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String TAG = "MainActivity";

    public static SpotifyPlayer spotifyPlayer = null;

    private String spotifyClientID = null;
    private String spotifyClientUri = null;
    private String oAuthToken = null;

    private boolean spotifyInstalled = false;

    private SpotifyApi spotifyApi = null;
    private SpotifyService spotifyService = null;

    /** View elements **/
    TextView textViewSettingsTitle;
    TextView textViewPlaylistsTitle;
    TextView textViewSettingsSpotifyStatus;
    TextView textViewSpotifyStatus;
    Switch switchAppActive;
    Switch switchAskEverytime;
    Spinner spinnerPlaylists;
    Button reloadSpotifyButton;
    Button reloadSpotifyPlaylistsButton;

    ArrayAdapter<String> spinnerPlaylistAdapter;

    /**
     * Create the main view
     *
     * @param savedInstanceState    Bundle      Android specific
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define view elements
        textViewSettingsTitle = (TextView) findViewById(R.id.textViewSettings);
        textViewPlaylistsTitle = (TextView) findViewById(R.id.textViewPlaylists);
        textViewSettingsSpotifyStatus = (TextView) findViewById(R.id.textViewSettingsSpotifyStatus);

        textViewSpotifyStatus = (TextView) findViewById(R.id.textViewSpotifyStatus);
        switchAppActive = (Switch) findViewById(R.id.switchAppActive);
        switchAskEverytime = (Switch) findViewById(R.id.switchListenEverytime);
        spinnerPlaylists = (Spinner) findViewById(R.id.spinnerPlaylists);
        reloadSpotifyButton = (Button) findViewById(R.id.reloadSpotifyButton);
        reloadSpotifyPlaylistsButton = (Button) findViewById(R.id.reloadSpotifyPlaylistsButton);

        spinnerPlaylists.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (playlistMap != null && position != 0) {
                    List<String> keys = new ArrayList<String>(playlistMap.keySet());

                    String playlistID = keys.get(position);

                    Settings.ACTIVE_PLAYLIST = playlistID;
                    saveSettings();

                    Toast.makeText(getBaseContext(), getString(R.string.spinner_selected_saved), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getBaseContext(), getString(R.string.spinner_selected_error), Toast.LENGTH_LONG);
            }

        });

        if (!MusicIntentReceiver.REGISTERD) {
            MusicIntentReceiver myReciever = new MusicIntentReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(myReciever, filter);
        }

        init();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SpotifyAuthHelper.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), SpotifyAppData.CLIENT_ID);

                spotifyClientID = playerConfig.clientId;
                oAuthToken = playerConfig.oauthToken;

                Settings.LAST_SPOTIFY_USERID = spotifyClientID;
                saveSettings();

                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {

                    @Override
                    public void onInitialized(SpotifyPlayer localSpotifyPlayer) {
                        spotifyPlayer = localSpotifyPlayer;
                        spotifyPlayer.addConnectionStateCallback(MainActivity.this);
                        spotifyPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                        Toast.makeText(getBaseContext(), getString(R.string.spotify_player_error), Toast.LENGTH_SHORT).show();
                    }

                });
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // Destroy database connections
        DbHelper.getInstance().destroy();

        // Destroy spotify
        Spotify.destroyPlayer(this);

        super.onDestroy();
    }

    /**
     * Initialize settings
     */
    private void init() {

        checkSpotifyApp();

        if (spotifyInstalled) {

            initSpotifyAuth();

            loadSettings();

            showSettings();

            if (getNetworkConnectivity(this) == Connectivity.OFFLINE) {
                onLoggedIn();
            }

        } else {
            textViewSpotifyStatus.setText(getString(R.string.spotify_not_installed));
        }
    }

    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    private void checkSpotifyApp() {
        // Spotify status
        spotifyInstalled = Utils.isPackageInstalled("com.spotify.music", this.getPackageManager());
    }

    private void initSpotifyAuth() {
        // Create SpotifyAuthHelper instance if spotify is installed
        SpotifyAuthHelper spotifyAuthHelper = new SpotifyAuthHelper();
        spotifyAuthHelper.createAuthRequest(this);

    }

    private void showSettings() {
        // App active
        switchAppActive.setChecked(Settings.APP_ACTIVE);
        switchAppActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.APP_ACTIVE = isChecked;
                saveSettings();
            }

        });

        // Ask everytime
        switchAskEverytime.setChecked(Settings.ASK_EVERYTIME);
        switchAskEverytime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.ASK_EVERYTIME = isChecked;
                saveSettings();
            }

        });
    }

    private void initSpotifyAPI() {
        if (spotifyApi == null) {
            // Load Spotify Web API
            spotifyApi = new SpotifyApi();
            spotifyApi.setAccessToken(oAuthToken);
        }

        if (spotifyService == null) {
            spotifyService = spotifyApi.getService();
        }

        spotifyService.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                Settings.LAST_SPOTIFY_USERNAME = spotifyClientUri = userPrivate.uri;
                saveSettings();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Error user private");
            }
        });
    }

    private void loadSettings() {
        Settings.loadSettings(this);
        Log.i(TAG, "Load settings");
    }

    private void saveSettings() {
        Settings.saveSettings(this);
        Log.i(TAG, "Save settings");
    }

    private void updatePlaylists(boolean forceReload) {
        Log.i(TAG, "Update playlists ...");

        if ((Settings.PLAYLIST_TIMESTAMP == -1 || (Utils.getTimestamp() - Settings.PLAYLIST_TIMESTAMP) > 24 * 60 * 60) && getNetworkConnectivity(this) != Connectivity.OFFLINE || forceReload && getNetworkConnectivity(this) != Connectivity.OFFLINE) {

            Log.i(TAG, "... need update");

            getAPIPlaylists(new Callback<Void>() {

                @Override
                public void success(Void aVoid, Response response) {
                    Log.i(TAG, "Get playlists from API");
                    updatePlaylistSpinner();
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.i(TAG, "Error from API");
                }

            });

        } else {
            Log.i(TAG, ".. don't need update, update spinner!");
            updatePlaylistSpinner();
        }
    }

    private Map<String, String> playlistMap = null;

    private void updatePlaylistSpinner() {
        Log.i(TAG, "Update spinner...");

        if (spinnerPlaylistAdapter == null) {
            spinnerPlaylistAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            spinnerPlaylistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        // Initialize Spotify playlist loader
        SpotifyPlaylistLoader playlistLoader = new SpotifyPlaylistLoader(this, new Callback<Map<String, String>>() {

            @Override
            public void success(Map<String, String> playlists, Response response) {
                playlistMap = playlists;

                int positionSelected = 0;
                Collection<String> playlistNames = playlists.values();

                spinnerPlaylistAdapter.clear();

                if (playlistNames.isEmpty()) {
                    spinnerPlaylistAdapter.add("-- Keine Playlists --");
                } else {
                    spinnerPlaylistAdapter.addAll(playlistNames);
                }

                spinnerPlaylists.setAdapter(spinnerPlaylistAdapter);

                String selectedPlaylistName = playlists.get(Settings.ACTIVE_PLAYLIST);

                if (selectedPlaylistName != null) {
                    positionSelected = spinnerPlaylistAdapter.getPosition(selectedPlaylistName);
                    spinnerPlaylists.setSelection(positionSelected);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }

        });

        // Execute loader
        playlistLoader.execute();
    }


    /**
     * Store a single playlist entry to local database
     *
     * @param values
     * @return
     */
    private void createPlaylistEntry(ContentValues values) {
        DbHelper.getInstance().createDatabaseHandler(this);
        DbHelperInsert insert = DbHelper.getInstance().insert();

        Log.i(TAG, "Inserted: " + values.valueSet().size());

        insert.setContentValues(values);
        insert.commit();

    }


    private void getAPIPlaylists(final Callback<Void> finishedCallback) {

        spotifyService.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {

            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {

                DbUtils.deleteAllPlaylists(getBaseContext());

                Log.i(TAG, "Count pager playlists: " + playlistSimplePager.items);

                // Store playlists local
                for (PlaylistSimple playlist : playlistSimplePager.items) {
                    String playlistID = playlist.id;
                    String playlistTitle = playlist.name;

                    ContentValues dbPlaylist = new ContentValues();

                    // Create new playlist entry in database
                    dbPlaylist.put(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_PLAYLIST_ID, playlistID);
                    dbPlaylist.put(SpotifyPlaylistContract.SpoitfyPlaylistEntry.COLUMN_NAME_TITLE, playlistTitle);

                    createPlaylistEntry(dbPlaylist);
                }

                // Update playlist timestamp in settings
                Settings.PLAYLIST_TIMESTAMP = Utils.getTimestamp();
                saveSettings();

                finishedCallback.success(null, response);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Error grabbing playlist " + error.getMessage());
            }

        });

    }

    /**
     *
     * @param playlistID
     */
    private void openSpotifyAndPlayMusic(String playlistID) {
        SpotifyHelper.openSpotify(this, spotifyClientUri, playlistID, true);
    }

    /**
     * Reload Spotify status
     *
     * @param view
     */
    public void reloadSpotifyButton(View view) {
        checkSpotifyApp();

        initSpotifyAuth();
    }

    /**
     * Reload Spotify playlists
     *
     * @param view
     */
    public void reloadSpotifyPlaylistsButton(View view) {
        updatePlaylists(true);
    }


    /* ==============================
     * SPOTIFY implementation
     * =============================== */
    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.i(TAG, "Spotify: OK!");
        }

        @Override
        public void onError(Error error) {
            Log.e(TAG, "Spotify: ERROR! " + error);
        }
    };

    private boolean isLoggedIn() {
        return spotifyPlayer != null && spotifyPlayer.isLoggedIn();
    }

    /**
     *
     */
    @Override
    public void onLoggedIn() {
        if (getNetworkConnectivity(this) != Connectivity.OFFLINE) {
            Settings.LAST_SPOTIFY_LOGIN = Utils.getTimestamp();
            saveSettings();

            initSpotifyAPI();

            updatePlaylists(false);

            textViewSpotifyStatus.setText(getString(R.string.spotify_logged_in));
            Log.i(TAG, "Spotify: Logged in!");
        }
        else if (getNetworkConnectivity(this) == Connectivity.OFFLINE && (Utils.getTimestamp() - Settings.LAST_SPOTIFY_LOGIN) < 24 * 60 * 60) {
            updatePlaylists(false);

            textViewSpotifyStatus.setText(getString(R.string.spotify_offline));
            Log.i(TAG, "Spotify: Logged in! (offline)");
        } else {
            textViewSpotifyStatus.setText(getString(R.string.spotify_login_failed));
        }
    }

    /**
     *
     */
    @Override
    public void onLoggedOut() {
        textViewSpotifyStatus.setText(getString(R.string.spotify_not_logged_in));
        Log.i(TAG, "Spotify: Logged out!");
    }

    /**
     *
     * @param error
     */
    @Override
    public void onLoginFailed(Error error) {
        textViewSpotifyStatus.setText(getString(R.string.spotify_login_failed));
        Log.i(TAG, "Spotify: Login failed! " + error);
    }

    /**
     *
     */
    @Override
    public void onTemporaryError() {
        Log.i(TAG, "Spotify: Temporary error!");
    }

    /**
     *
     * @param s
     */
    @Override
    public void onConnectionMessage(String s) {
        Log.i(TAG, "Spotify connection message: " + s);
    }

    /**
     *
     * @param playerEvent
     */
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.i(TAG, "Spotify playback event");
    }

    /**
     *
     * @param error
     */
    @Override
    public void onPlaybackError(Error error) {
        Log.i(TAG, "Spotify playback error: " + error);
    }
}
