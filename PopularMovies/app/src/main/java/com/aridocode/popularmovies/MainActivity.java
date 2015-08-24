package com.aridocode.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;

import com.aridocode.popularmovies.data.MoviesContract;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private static final String DETAIL_FRAGMENT_TAG = "DF_TAG";

    private boolean mTwoPane;

    private View mViewDetailContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailActivityFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }

            mViewDetailContainer = findViewById(R.id.movie_detail_container);
            mViewDetailContainer.setVisibility(View.GONE);
        } else {
            mTwoPane = false;
        }

        getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI,
                MoviesContract.MoviesEntry.COLUMN_FAVORITED + " = ?",
                new String[]{"0"});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onMovieSelected(Uri movieUri) {
        if (mTwoPane) {
            mViewDetailContainer.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, MovieDetailActivityFragment.newInstance(movieUri), DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class).setData(movieUri);
            startActivity(intent);
        }
    }
}
