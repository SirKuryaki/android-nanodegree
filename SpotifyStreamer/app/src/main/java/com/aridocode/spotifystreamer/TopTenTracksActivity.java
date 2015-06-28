package com.aridocode.spotifystreamer;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aridocode.spotifystreamer.data.SpotifyArtist;


public class TopTenTracksActivity extends AppCompatActivity {

    public static final String EXTRA_ARTIST = "extra_artist";

    private SpotifyArtist artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ARTIST)) {
            artist = intent.getParcelableExtra(EXTRA_ARTIST);
        }

        if (artist != null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(artist.getName());
            }
        }
    }

}
