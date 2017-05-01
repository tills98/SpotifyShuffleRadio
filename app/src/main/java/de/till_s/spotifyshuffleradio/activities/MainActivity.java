package de.till_s.spotifyshuffleradio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.till_s.spotifyshuffleradio.R;
import de.till_s.spotifyshuffleradio.Settings;
import de.till_s.spotifyshuffleradio.helper.db.DbHelper;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyHelper;
import de.till_s.spotifyshuffleradio.service.BootService;
import de.till_s.spotifyshuffleradio.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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

    private SpotifyHelper spotifyHelper = null;

    /**
     * Create the main view
     *
     * @param savedInstanceState    Bundle      Android specific
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spotifyHelper = new SpotifyHelper(this, this, textViewSpotifyStatus);

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
                Map<String, String> playlistMap = spotifyHelper.getPlaylistCache();

                if (playlistMap != null && position != 0) {
                    List<String> keys = new ArrayList<String>(playlistMap.keySet());

                    Settings.ACTIVE_PLAYLIST = keys.get(position);
                    saveSettings();

                    Toast.makeText(getBaseContext(), getString(R.string.spinner_selected_saved), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getBaseContext(), getString(R.string.spinner_selected_error), Toast.LENGTH_LONG).show();
            }

        });

        Intent bootServiceIntent = new Intent(this, BootService.class);
        startService(bootServiceIntent);

        Log.i(TAG, "Create");


        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        spotifyHelper.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Pause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Resume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "Destroy");

        // Destroy database connections
        DbHelper.getInstance().destroy();

        spotifyHelper.destroy();

        super.onDestroy();
    }

    /**
     * Initialize settings
     */
    private void init() {

        spotifyHelper.setSpotifyLoggedInCallback(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                Settings.LAST_SPOTIFY_LOGIN = Utils.getTimestamp();

                textViewSpotifyStatus.setText(getString(R.string.spotify_logged_in));

                spotifyHelper.loadPlaylists(spinnerPlaylists, false);
                Log.i(TAG, "Spotify: Logged in!");
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        spotifyHelper.setSpotifyOfflineCallback(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                spotifyHelper.loadPlaylists(spinnerPlaylists, false);
                textViewSpotifyStatus.setText(getString(R.string.spotify_offline));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        spotifyHelper.setSpotifyLoginFailedCallback(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                textViewSpotifyStatus.setText(getString(R.string.spotify_login_failed));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });


        loadSettings();
        showSettings();

        spotifyHelper.init();
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



    private void loadSettings() {
        Settings.loadSettings(this);
        Log.i(TAG, "Load settings");
    }

    private void saveSettings() {
        Settings.saveSettings(this);
        Log.i(TAG, "Save settings");
    }


    /**
     * Reload Spotify status
     *
     * @param view
     */
    public void reloadSpotifyButton(View view) {
        spotifyHelper.reloadLogin();
    }

    /**
     * Reload Spotify playlists
     *
     * @param view
     */
    public void reloadSpotifyPlaylistsButton(View view) {
        spotifyHelper.reloadPlaylists(spinnerPlaylists);
    }

}
