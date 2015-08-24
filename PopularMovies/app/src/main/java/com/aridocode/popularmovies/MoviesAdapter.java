package com.aridocode.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by sirkuryaki on 26/7/15.
 */
class MoviesAdapter extends CursorAdapter {

    private final Context mContext;
    private final Picasso picasso;
    private final boolean isConnected;

    public MoviesAdapter(Context c) {
        super(c, null, 0);

        mContext = c;
        picasso = Picasso.with(c);

        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);
        imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(8, 8, 8, 8);
        imageView.setAdjustViewBounds(true);

        return imageView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String url = "http://image.tmdb.org/t/p/w185/" +
                cursor.getString(MainActivityFragment.COL_POSTER_PATH);

        picasso.load(url)
                .networkPolicy(isConnected ? NetworkPolicy.NO_CACHE :NetworkPolicy.OFFLINE)
                .into((ImageView) view);
    }

}