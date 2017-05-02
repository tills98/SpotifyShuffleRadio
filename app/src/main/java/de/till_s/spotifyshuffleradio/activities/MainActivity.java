package de.till_s.spotifyshuffleradio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.till_s.spotifyshuffleradio.R;
import de.till_s.spotifyshuffleradio.helper.db.DbHelper;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyHelper;
import de.till_s.spotifyshuffleradio.service.BootService;
import de.till_s.spotifyshuffleradio.utils.Settings;
import de.till_s.spotifyshuffleradio.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SpotifyHelper spotifyHelper;

    /**
     * Views
     **/
    TextView textViewTitleSpotifyState;
    TextView textViewSpotifyState;
    Button buttonRefreshSpotifyState;

    TextView textViewPlaylists;
    Spinner spinnerPlaylists;
    Button buttonRefreshPlaylists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Class activityClass = null;
        if (id == R.id.nav_settings) {
            activityClass = SettingsActivity.class;
        }

        Intent intent = new Intent(this, activityClass);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        startActivity(intent);

        return true;
    }


    private void init() {

        loadSettings();

        declareViews();

        loadServices();

        initSpotifyHelper();

    }

    private void declareViews() {
        textViewTitleSpotifyState = (TextView) findViewById(R.id.textViewTitleSpotifyState);
        textViewSpotifyState = (TextView) findViewById(R.id.textViewSpotifyState);
        buttonRefreshSpotifyState = (Button) findViewById(R.id.buttonRefreshSpotifyState);

        textViewPlaylists = (TextView) findViewById(R.id.textViewTitlePlaylists);
        spinnerPlaylists = (Spinner) findViewById(R.id.spinnerPlaylists);
        buttonRefreshPlaylists = (Button) findViewById(R.id.buttonPlaylistsRefresh);
    }

    private void registerSpinnerListener() {
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
    }

    private void unregisterSpinnerListener() {
        spinnerPlaylists.setOnItemSelectedListener(null);
    }

    private void initSpotifyHelper() {
        spotifyHelper = new SpotifyHelper(this, this);

        spotifyHelper.setSpotifyLoggedInCallback(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                Settings.LAST_SPOTIFY_LOGIN = Utils.getTimestamp();

                textViewSpotifyState.setText(getString(R.string.spotify_logged_in));

                unregisterSpinnerListener();

                spotifyHelper.loadPlaylists(spinnerPlaylists);

                registerSpinnerListener();

                Log.i(TAG, "Spotify: Logged in!");
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        spotifyHelper.setSpotifyOfflineCallback(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                unregisterSpinnerListener();
                spotifyHelper.loadPlaylists(spinnerPlaylists);
                registerSpinnerListener();

                textViewSpotifyState.setText(getString(R.string.spotify_offline));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        spotifyHelper.setSpotifyLoginFailedCallback(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                textViewSpotifyState.setText(getString(R.string.spotify_login_failed));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        spotifyHelper.init();
    }

    private void loadServices() {
        Intent bootServiceIntent = new Intent(this, BootService.class);
        startService(bootServiceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        spotifyHelper.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        DbHelper.getInstance().destroy();

        spotifyHelper.destroy();

        super.onDestroy();
    }


    private void saveSettings() {
        Settings.saveSettings(this);
    }

    private void loadSettings() {
        Settings.loadSettings(this);
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
