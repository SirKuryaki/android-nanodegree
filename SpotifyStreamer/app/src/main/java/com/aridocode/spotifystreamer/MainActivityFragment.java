package com.aridocode.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aridocode.spotifystreamer.data.SpotifyArtist;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;


public class MainActivityFragment extends Fragment {

    private static final String ARG_LAST_SEARCH = "lastSearch";

    private ArrayAdapter<SpotifyArtist> mResultsAdapter;
    private ArrayList<SpotifyArtist> mLastSearch;
    private OnArtistSelectedListener mListener;

    public MainActivityFragment() {
        mLastSearch = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mLastSearch = savedInstanceState.getParcelableArrayList(ARG_LAST_SEARCH);
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        EditText etxtSearch = (EditText) rootView.findViewById(R.id.etxt_search_artist);
        etxtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = v.getText().toString();

                    if (query.length() > 0) {
                        FetchArtistsTask task = new FetchArtistsTask();
                        task.execute(query);
                        v.clearFocus();
                        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        mResultsAdapter = new ArtistAdapter(getActivity());
        ListView listView = (ListView) rootView.findViewById(R.id.listview_artists);
        listView.setAdapter(mResultsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyArtist artist = (SpotifyArtist) parent.getItemAtPosition(position);
                if (mListener != null) {
                    mListener.onArtistSelected(artist);
                }
            }
        });

        if (mLastSearch != null) {
            mResultsAdapter.addAll(mLastSearch);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_LAST_SEARCH, mLastSearch);
    }

    private void showToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = null;
        try {
            mListener = (OnArtistSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnArtistSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnArtistSelectedListener {
        void onArtistSelected(SpotifyArtist artist);
    }

    private class FetchArtistsTask extends AsyncTask<String, Void, List<SpotifyArtist>> {

        @Override
        protected List<SpotifyArtist> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            List<SpotifyArtist> artists;
            try {
                ArtistsPager results = spotify.searchArtists(params[0]);
                artists = new ArrayList<>();

                if (results.artists.items != null) {
                    for (Artist artist : results.artists.items) {
                        SpotifyArtist spotifyArtist = new SpotifyArtist(artist.id, artist.name);

                        List<Image> images = artist.images;
                        if (images != null && !images.isEmpty()) {
                            /*
                             * This array can contain up to three images sorted by size descending.
                             * Last image is very small (64x64) so we use the middle one.
                             */
                            if (images.size() > 1) {
                                spotifyArtist.setImage(images.get(1).url);
                            } else {
                                spotifyArtist.setImage(images.get(0).url);
                            }
                        }

                        artists.add(spotifyArtist);
                    }
                }

            } catch (RetrofitError error) {
                artists = null;
            }

            return artists;
        }

        @Override
        protected void onPostExecute(List<SpotifyArtist> artists) {
            if (artists == null) {
                showToast(R.string.search_error);
            } else {
                mResultsAdapter.clear();
                mResultsAdapter.addAll(artists);
                mLastSearch = new ArrayList<>(artists);

                if (artists.size() == 0) {
                    showToast(R.string.search_no_results);
                }
            }
        }
    }

    public class ArtistAdapter extends ArrayAdapter<SpotifyArtist> {

        private final LayoutInflater mInflater;
        private final Picasso picasso;

        public ArtistAdapter(Context context) {
            super(context, 0);
            mInflater = LayoutInflater.from(context);
            picasso = Picasso.with(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            ViewHolder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.list_item_search_artist, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            SpotifyArtist artist = getItem(position);
            holder.name.setText(artist.getName());

            if (artist.getImage() != null) {
                picasso.load(artist.getImage()).into(holder.cover);
                holder.cover.setVisibility(View.VISIBLE);
            } else {
                holder.cover.setVisibility(View.INVISIBLE);
            }

            return view;
        }

        public class ViewHolder {
            private final ImageView cover;
            private final TextView name;

            public ViewHolder(View view) {
                this.cover = (ImageView) view.findViewById(R.id.list_item_cover);
                this.name = (TextView) view.findViewById(R.id.list_item_result_textview);
            }
        }
    }
}
