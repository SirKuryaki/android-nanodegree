package com.aridocode.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aridocode.spotifystreamer.data.SpotifyArtist;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnArtistSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onArtistSelected(SpotifyArtist artist) {
        Intent intent = new Intent(this, TopTenTracksActivity.class);
        intent.putExtra(TopTenTracksActivity.EXTRA_ARTIST, artist);
        startActivity(intent);
    }
}
