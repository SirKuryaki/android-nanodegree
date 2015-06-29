package com.aridocode.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aridocode.spotifystreamer.data.SpotifyArtist;
import com.aridocode.spotifystreamer.data.SpotifyTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class TopTenTracksActivityFragment extends Fragment {

    private SpotifyArtist artist;
    private ArrayList<SpotifyTrack> mTracks = null;
    private TracksAdapter mAdapter;

    public TopTenTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(TopTenTracksActivity.EXTRA_ARTIST)) {
            artist = intent.getParcelableExtra(TopTenTracksActivity.EXTRA_ARTIST);
        }

        if (artist != null && mTracks == null) {
            FetchTracksTask task = new FetchTracksTask();
            task.execute(artist.getId());
        }

        mAdapter = new TracksAdapter(getActivity());
        ListView listView = (ListView) rootView.findViewById(R.id.listview_top_tracks);
        listView.setAdapter(mAdapter);

        if (mTracks != null) {
            mAdapter.addAll(mTracks);
        }

        return rootView;
    }

    private void showToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    private class FetchTracksTask extends AsyncTask<String, Void, List<SpotifyTrack>> {

        @Override
        protected List<SpotifyTrack> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            Map<String, Object> options = new HashMap<>();
            options.put("country", "US");
            SpotifyService spotify = api.getService();
            List<SpotifyTrack> tracks;
            try {
                Tracks results = spotify.getArtistTopTrack(params[0], options);
                tracks = new ArrayList<>();

                if (results.tracks != null) {
                    for (Track track : results.tracks) {

                        String cover;
                        String thumb;

                        AlbumSimple album = track.album;

                        List<Image> images = album.images;
                        if (images != null && !images.isEmpty()) {
                            /*
                             * This array can contain up to three images sorted by size descending.
                             * Last image is very small (64x64) so we use the middle one for thumbnail
                             * and the first image for cover.
                             */
                            if (images.size() > 1) {
                                cover = images.get(0).url;
                                thumb = images.get(1).url;
                            } else {
                                cover = images.get(0).url;
                                thumb = images.get(0).url;
                            }
                        } else {
                            cover = null;
                            thumb = null;
                        }

                        SpotifyTrack spotifyTrack = new SpotifyTrack(track.name, album.name, thumb, cover, track.uri);
                        tracks.add(spotifyTrack);
                    }
                }

            } catch (RetrofitError error) {
                tracks = null;
            }

            return tracks;
        }

        @Override
        protected void onPostExecute(List<SpotifyTrack> tracks) {
            if (tracks == null) {
                showToast(R.string.search_error);
            } else {
                mAdapter.clear();
                mAdapter.addAll(tracks);
                mTracks = new ArrayList<>(tracks);

                if (tracks.size() == 0) {
                    showToast(R.string.search_no_results);
                }
            }
        }
    }

    public class TracksAdapter extends ArrayAdapter<SpotifyTrack> {

        private final LayoutInflater mInflater;
        private final Picasso picasso;

        public TracksAdapter(Context context) {
            super(context, 0);
            mInflater = LayoutInflater.from(context);
            picasso = Picasso.with(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            ViewHolder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.list_item_top_tracks, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            SpotifyTrack track = getItem(position);
            holder.name.setText(track.getName());
            holder.album.setText(track.getAlbum());

            if (track.getThumbnail() != null) {
                picasso.load(track.getThumbnail()).into(holder.cover);
                holder.cover.setVisibility(View.VISIBLE);
            } else {
                holder.cover.setVisibility(View.INVISIBLE);
            }

            return view;
        }

        public class ViewHolder {
            private final ImageView cover;
            private final TextView name;
            private final TextView album;

            public ViewHolder(View view) {
                this.cover = (ImageView) view.findViewById(R.id.list_item_cover);
                this.name = (TextView) view.findViewById(R.id.list_item_name_textview);
                this.album = (TextView) view.findViewById(R.id.list_item_album_textview);
            }
        }
    }
}
