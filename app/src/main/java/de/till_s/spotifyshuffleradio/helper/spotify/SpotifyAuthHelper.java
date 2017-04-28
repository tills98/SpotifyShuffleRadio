package de.till_s.spotifyshuffleradio.helper.spotify;

import android.app.Activity;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import de.till_s.spotifyshuffleradio.utils.SpotifyAppData;

/**
 * Created by Till on 23.04.2017.
 */

public class SpotifyAuthHelper {

    public static final int REQUEST_CODE = 1337;

    public void createAuthRequest(Activity activity) {
        AuthenticationRequest.Builder authReqBuilder = new AuthenticationRequest.Builder(SpotifyAppData.CLIENT_ID, AuthenticationResponse.Type.TOKEN, SpotifyAppData.REDIRECT_URI);

        authReqBuilder.setScopes(new String[] {
                "user-read-private",
                "streaming",
                "playlist-read-private"
        });
        AuthenticationRequest request = authReqBuilder.build();

        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);
    }

}
