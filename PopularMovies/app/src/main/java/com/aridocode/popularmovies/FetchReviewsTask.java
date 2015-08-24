package com.aridocode.popularmovies;

import android.content.ContentValues;
import android.content.Context;
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
class FetchReviewsTask extends AsyncTask<Long, Void, Void> {

    private static final String TAG_RESULTS = "results";

    private static final String TAG_ID = "id";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_CONTENT = "content";

    private final Context mContext;

    public FetchReviewsTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Long... params) {

        if (params.length == 0) {
            return null;
        }
        long movieId = params[0];


        final String API_KEY_PARAM = "api_key";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority(Constants.BASE_URL)
                .appendPath("3")
                .appendPath("movie")
                .appendPath(Long.toString(movieId))
                .appendPath("reviews")
                .appendQueryParameter(API_KEY_PARAM, Constants.THE_MOVIE_DB_API_KEY);


        JSONObject jsonRoot = WSHelper.getJSONFromUri(builder.build());

        try {
            getReviewsFromJson(jsonRoot, movieId);
        } catch (JSONException ignored) {

        }

        return null;
    }


    private void getReviewsFromJson(JSONObject jsonRoot, long movieId)
            throws JSONException {

        if (jsonRoot == null) {
            return;
        }

        JSONArray resultsArray = jsonRoot.getJSONArray(TAG_RESULTS);

        Vector<ContentValues> cVVector = new Vector<>(resultsArray.length());

        for (int i = 0; i < resultsArray.length(); i++) {

            JSONObject trailerObject = resultsArray.getJSONObject(i);

            String id = trailerObject.getString(TAG_ID);
            String author = trailerObject.getString(TAG_AUTHOR);
            String content = trailerObject.getString(TAG_CONTENT);

            ContentValues trailerValues = new ContentValues();

            trailerValues.put(MoviesContract.ReviewsEntry.COLUMN_REVIEW_ID, id);
            trailerValues.put(MoviesContract.ReviewsEntry.COLUMN_AUTHOR, author);
            trailerValues.put(MoviesContract.ReviewsEntry.COLUMN_CONTENT, content);
            trailerValues.put(MoviesContract.ReviewsEntry.COLUMN_MOVIE_ID, movieId);

            cVVector.add(trailerValues);
        }

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(MoviesContract.ReviewsEntry.CONTENT_URI, cvArray);
        }
    }
}
