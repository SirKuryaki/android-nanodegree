package com.aridocode.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.aridocode.popularmovies.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by sirkuryaki on 26/7/15.
 */
class FetchMoviesTask extends AsyncTask<String, Void, Void> {

    private static final String TAG_RESULTS = "results";

    private static final String TAG_ID = "id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_OVERVIEW = "overview";
    private static final String TAG_RELEASE_DATE = "release_date";
    private static final String TAG_POSTER_PATH = "poster_path";
    private static final String TAG_VOTE_AVERAGE = "vote_average";

    private final Context mContext;

    public FetchMoviesTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        final String SORT_PARAM = "sort_by";
        final String API_KEY_PARAM = "api_key";

        String sortParam = params[0];

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority(Constants.BASE_URL)
                .appendPath("3")
                .appendPath("discover")
                .appendPath("movie")
                .appendQueryParameter(SORT_PARAM, sortParam)
                .appendQueryParameter(API_KEY_PARAM, Constants.THE_MOVIE_DB_API_KEY);


        JSONObject jsonRoot = WSHelper.getJSONFromUri(builder.build());

        try {
            getWeatherDataFromJson(jsonRoot, sortParam);
        } catch (JSONException ignored) {

        }

        return null;
    }

    private void getWeatherDataFromJson(JSONObject jsonRoot, String sortParam)
            throws JSONException {

        if (jsonRoot == null) {
            return;
        }

        JSONArray resultsArray = jsonRoot.getJSONArray(TAG_RESULTS);

        Vector<ContentValues> cVVector = new Vector<>(resultsArray.length());
        Vector<ContentValues> cVResultVector = new Vector<>(resultsArray.length());

        for (int i = 0; i < resultsArray.length(); i++) {


            JSONObject movieObject = resultsArray.getJSONObject(i);

            int id = movieObject.getInt(TAG_ID);

            Cursor movieCursor = mContext.getContentResolver().query(
                    MoviesContract.MoviesEntry.CONTENT_URI,
                    new String[]{MoviesContract.MoviesEntry._ID},
                    MoviesContract.MoviesEntry._ID + " = ?",
                    new String[]{Integer.toString(id)},
                    null);

            if (!movieCursor.moveToFirst()) {
                String title = movieObject.getString(TAG_TITLE);
                String overview = movieObject.getString(TAG_OVERVIEW);
                String releaseDate = movieObject.getString(TAG_RELEASE_DATE);
                String poster = movieObject.getString(TAG_POSTER_PATH);
                Double rating = movieObject.getDouble(TAG_VOTE_AVERAGE);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesContract.MoviesEntry._ID, id);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, title);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, poster);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, rating);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_FAVORITED, 0);

                cVVector.add(movieValues);
            }
            movieCursor.close();

            ContentValues resultValues = new ContentValues();
            resultValues.put(MoviesContract.ListResultEntry.COLUMN_MOVIE_ID, id);
            resultValues.put(MoviesContract.ListResultEntry.COLUMN_ORDER, i);
            resultValues.put(MoviesContract.ListResultEntry.COLUMN_FILTER_TYPE, sortParam);
            cVResultVector.add(resultValues);
        }

        if (cVResultVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cvArray);

            cvArray = new ContentValues[cVResultVector.size()];
            cVResultVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(MoviesContract.ListResultEntry.CONTENT_URI, cvArray);
        }
    }
}
