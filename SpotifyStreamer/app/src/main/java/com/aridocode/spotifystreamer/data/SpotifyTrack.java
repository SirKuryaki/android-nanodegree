package com.aridocode.spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sirkuryaki on 6/28/15.
 */
public class SpotifyTrack implements Parcelable {

    private final String name;
    private final String album;
    private final String thumbnail;
    private final String cover;
    private final String uri;

    public SpotifyTrack(String name, String album, String thumbnail, String cover, String uri) {
        this.name = name;
        this.album = album;
        this.thumbnail = thumbnail;
        this.cover = cover;
        this.uri = uri;
    }

    private SpotifyTrack(Parcel in) {
        this.name = in.readString();
        this.album = in.readString();
        this.thumbnail = in.readString();
        this.cover = in.readString();
        this.uri = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.name);
        dest.writeString(this.album);
        dest.writeString(this.thumbnail);
        dest.writeString(this.cover);
        dest.writeString(this.uri);
    }

    public static final Parcelable.Creator<SpotifyTrack> CREATOR
            = new Parcelable.Creator<SpotifyTrack>() {
        public SpotifyTrack createFromParcel(Parcel in) {
            return new SpotifyTrack(in);
        }

        public SpotifyTrack[] newArray(int size) {
            return new SpotifyTrack[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getAlbum() {
        return album;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getCover() {
        return cover;
    }

    public String getUri() {
        return uri;
    }
}
