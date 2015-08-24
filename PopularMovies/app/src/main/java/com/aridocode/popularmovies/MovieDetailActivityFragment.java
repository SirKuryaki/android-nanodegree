package com.aridocode.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aridocode.popularmovies.data.MoviesContract;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.squareup.picasso.Picasso;


public class MovieDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String DETAIL_URI = "URI";

    private static final String[] MOVIES_COLUMNS = {
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_FAVORITED
    };

    private static final int COL_MOVIE_TITLE = 0;
    private static final int COL_MOVIE_RELEASE_DATE = 1;
    private static final int COL_MOVIE_VOTE_AVERAGE = 2;
    private static final int COL_MOVIE_POSTER_PATH = 3;
    private static final int COL_MOVIE_OVERVIEW = 4;
    private static final int COL_FAVORITED = 5;


    private static final String[] TRAILER_COLUMNS = {
            MoviesContract.TrailersEntry.COLUMN_NAME,
            MoviesContract.TrailersEntry.COLUMN_KEY
    };

    private static final int COL_TRAILER_NAME = 0;
    private static final int COL_TRAILER_KEY = 1;

    private static final String[] REVIEWS_COLUMNS = {
            MoviesContract.ReviewsEntry.COLUMN_CONTENT,
            MoviesContract.ReviewsEntry.COLUMN_AUTHOR
    };

    private static final int COL_REVIEW_CONTENT = 0;
    private static final int COL_REVIEW_AUTHOR = 1;

    private static final int MOVIE_LOADER = 0;
    private static final int TRAILERS_LOADER = 1;
    private static final int REVIEWS_LOADER = 2;

    private TextView mTxtTitle;

    private ImageView mImgMovieCover;
    private TextView mTxtYear;
    private TextView mTxtRating;
    private TextView mTxtSynopsis;

    private ViewGroup mGroupTrailer;
    private ViewGroup mGroupReviews;

    private Button mBtnFavorited;

    private Uri mUri;

    private ShareActionProvider mShareActionProvider;

    private Menu mMenu;

    public static MovieDetailActivityFragment newInstance(Uri movieUri) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(DETAIL_URI, movieUri);

        MovieDetailActivityFragment fragment = new MovieDetailActivityFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DETAIL_URI);
        }

        mTxtTitle = (TextView) rootView.findViewById(R.id.txt_detail_title);
        mImgMovieCover = (ImageView) rootView.findViewById(R.id.img_movie_poster);
        mTxtYear = (TextView) rootView.findViewById(R.id.txt_movie_year);
        mTxtRating = (TextView) rootView.findViewById(R.id.txt_movie_rating);
        mTxtSynopsis = (TextView) rootView.findViewById(R.id.txt_movie_plot);

        mBtnFavorited = (Button) rootView.findViewById(R.id.btn_movie_favorite);

        mGroupTrailer = (ViewGroup) rootView.findViewById(R.id.lyt_container_trailer);
        mGroupReviews = (ViewGroup) rootView.findViewById(R.id.lyt_container_reviews);

        mBtnFavorited.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = new ShareActionProvider(getActivity());
        MenuItemCompat.setActionProvider(item, mShareActionProvider);

        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mUri != null) {
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
            getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
            getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mUri != null) {
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
            getLoaderManager().restartLoader(TRAILERS_LOADER, null, this);
            getLoaderManager().restartLoader(REVIEWS_LOADER, null, this);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        updateMovieDetail();
    }

    private void updateMovieDetail() {
        if (mUri != null) {
            long movieId = ContentUris.parseId(mUri);
            FetchTrailersTask task = new FetchTrailersTask(getActivity());
            task.execute(movieId);

            FetchReviewsTask reviewsTask = new FetchReviewsTask(getActivity());
            reviewsTask.execute(movieId);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MOVIE_LOADER) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    MOVIES_COLUMNS,
                    null,
                    null,
                    null
            );
        } else if (id == TRAILERS_LOADER) {
            long movieId = ContentUris.parseId(mUri);
            return new CursorLoader(
                    getActivity(),
                    MoviesContract.TrailersEntry.buildMovieTrailersUri(movieId),
                    TRAILER_COLUMNS,
                    null,
                    null,
                    null
            );
        } else if (id == REVIEWS_LOADER) {
            long movieId = ContentUris.parseId(mUri);
            return new CursorLoader(
                    getActivity(),
                    MoviesContract.ReviewsEntry.buildMovieReviewsUri(movieId),
                    REVIEWS_COLUMNS,
                    null,
                    null,
                    null
            );
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id == MOVIE_LOADER) {
            onMovieQueryComplete(data);
        } else if (id == TRAILERS_LOADER) {
            onTrailersQueryComplete(data);
        } else if (id == REVIEWS_LOADER) {
            onReviewsQueryComplete(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void onMovieQueryComplete(Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            return;
        }

        String url = "http://image.tmdb.org/t/p/w185/" + cursor.getString(COL_MOVIE_POSTER_PATH);

        String release = cursor.getString(COL_MOVIE_RELEASE_DATE);

        mTxtYear.setText(release.split("-")[0]);
        mTxtTitle.setText(cursor.getString(COL_MOVIE_TITLE));
        mTxtRating.setText(String.format(getString(R.string.rating_format), cursor.getDouble(COL_MOVIE_VOTE_AVERAGE)));
        mTxtSynopsis.setText(cursor.getString(COL_MOVIE_OVERVIEW));

        if (cursor.getInt(COL_FAVORITED) == 0) {
            mBtnFavorited.setText(R.string.favorite);
        } else {
            mBtnFavorited.setText(R.string.favorited);
        }

        Picasso.with(getActivity()).load(url).into(mImgMovieCover);
    }

    private void onTrailersQueryComplete(Cursor cursor) {

        for (int i = mGroupTrailer.getChildCount() - 1; i >= 1; i--) {
            mGroupTrailer.removeViewAt(i);
        }

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        cursor.moveToPosition(-1);
        boolean hasTrailer = false;
        while (cursor.moveToNext()) {

            final String videoId = cursor.getString(COL_TRAILER_KEY);

            final View trailerView = inflater.inflate(R.layout.list_item_movie_trailer, mGroupTrailer, false);
            final TextView txtTrailer = (TextView) trailerView.findViewById(R.id.txt_trailer_name);

            txtTrailer.setText(cursor.getString(COL_TRAILER_NAME));

            txtTrailer.setEnabled(true);
            txtTrailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(), Constants.DEVELOPER_KEY, videoId);
                    startActivity(intent);
                }
            });

            mGroupTrailer.addView(trailerView);

            if (!hasTrailer && mShareActionProvider != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + videoId);
                shareIntent.setType("text/plain");
                mShareActionProvider.setShareIntent(shareIntent);

                hasTrailer = true;
            }
        }

        if( mMenu != null ) {
            MenuItem share = mMenu.findItem(R.id.menu_item_share);

            if (hasTrailer) {
                share.setVisible(true);
            } else {
                share.setVisible(false);
            }
        }
    }

    private void onReviewsQueryComplete(Cursor cursor) {

        for (int i = mGroupReviews.getChildCount() - 1; i >= 1; i--) {
            mGroupReviews.removeViewAt(i);
        }

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {

            final View trailerView = inflater.inflate(R.layout.list_item_movie_review, mGroupReviews, false);
            final TextView txtAuthor = (TextView) trailerView.findViewById(R.id.txt_review_author);
            final TextView txtComment = (TextView) trailerView.findViewById(R.id.txt_review_comment);

            txtAuthor.setText(cursor.getString(COL_REVIEW_AUTHOR));
            txtComment.setText(cursor.getString(COL_REVIEW_CONTENT));

            mGroupReviews.addView(trailerView);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_movie_favorite) {
            Button button = (Button) v;

            ContentValues contentValues = new ContentValues();

            if (button.getText().equals(getString(R.string.favorited))) {
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_FAVORITED, 0);
            } else {
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_FAVORITED, 1);
            }

            getActivity().getContentResolver().update(mUri, contentValues, null, null);
        }
    }
}
