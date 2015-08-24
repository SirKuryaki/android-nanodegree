package com.aridocode.popularmovies;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;

import com.aridocode.popularmovies.data.MoviesContract;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {

    private static final String[] MOVIES_COLUMNS = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH
    };

    @SuppressWarnings("unused")
    static final int COL_MOVIE_ID = 0;
    static final int COL_POSTER_PATH = 1;

    private static final int MOVIES_LOADER = 0;

    private MoviesAdapter mAdapter;
    private GridView mGridView;

    private Callback mListener;

    private String mFilter = "popularity.desc";

    private static final String SELECTED_KEY = "selected_position";
    private int mPosition = GridView.INVALID_POSITION;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String title = (String) parent.getItemAtPosition(position);

        switch (title) {
            case "Most Popular": {
                showListFilter("popularity.desc");
                FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity());
                moviesTask.execute(mFilter);
                break;
            }
            case "Highest Rated": {
                showListFilter("vote_average.desc");
                FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity());
                moviesTask.execute(mFilter);
                break;
            }
            default:
                showListFilter("favorites");
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface Callback {
        void onMovieSelected(Uri movieUri);
    }

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mAdapter = new MoviesAdapter(getActivity());
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onMovieSelected(MoviesContract.MoviesEntry.buildMovieUri(id));
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_order, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        return rootView;
    }

    private void showListFilter(String filter) {
        mFilter = filter;
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = null;
        try {
            mListener = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MainActivityFragment.Callback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (mFilter.equals("favorites")) {
            return new CursorLoader(
                    getActivity(),
                    MoviesContract.MoviesEntry.CONTENT_URI,
                    MOVIES_COLUMNS,
                    MoviesContract.MoviesEntry.COLUMN_FAVORITED + " = 1",
                    null,
                    null
            );
        } else {
            String sortOrder = MoviesContract.ListResultEntry.COLUMN_ORDER + " ASC";
            return new CursorLoader(
                    getActivity(),
                    MoviesContract.ListResultEntry.CONTENT_URI,
                    MOVIES_COLUMNS,
                    MoviesContract.ListResultEntry.COLUMN_FILTER_TYPE + " = '" + mFilter + "'",
                    null,
                    sortOrder
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
