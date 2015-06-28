package com.aridocode.spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sirkuryaki on 6/27/15.
 */
public class SpotifyArtist implements Parcelable {
    private final String id;
    private final String name;
    private String image;

    public SpotifyArtist(String id, String name) {
        this.id = id;
        this.name = name;
    }

    private SpotifyArtist(Parcel in){
        this.id = in.readString();
        this.name = in.readString();
        this.image = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.id,
                this.name,
                this.image});
    }

    public static final Parcelable.Creator<SpotifyArtist> CREATOR
            = new Parcelable.Creator<SpotifyArtist>() {
        public SpotifyArtist createFromParcel(Parcel in) {
            return new SpotifyArtist(in);
        }

        public SpotifyArtist[] newArray(int size) {
            return new SpotifyArtist[size];
        }
    };


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
