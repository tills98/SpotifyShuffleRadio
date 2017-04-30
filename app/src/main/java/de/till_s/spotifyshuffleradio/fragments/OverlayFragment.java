package de.till_s.spotifyshuffleradio.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.till_s.spotifyshuffleradio.R;
import de.till_s.spotifyshuffleradio.Settings;
import de.till_s.spotifyshuffleradio.helper.spotify.SpotifyHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverlayFragment extends Fragment {


    public static Fragment newInstance() {
        return new OverlayFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overlay, container, false);

        final Context context = view.getContext();

        Button yesButton = (Button) view.findViewById(R.id.buttonOverlayYes);
        yesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Settings.LAST_SPOTIFY_USERURI != null && Settings.ACTIVE_PLAYLIST != null) {
                    SpotifyHelper.openSpotify(context, Settings.LAST_SPOTIFY_USERURI, Settings.ACTIVE_PLAYLIST, true);
                }
                getActivity().finish();
            }

        });

        Button noButton = (Button) view.findViewById(R.id.buttonOverlayNo);
        noButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }

        });

        return view;
    }

}
