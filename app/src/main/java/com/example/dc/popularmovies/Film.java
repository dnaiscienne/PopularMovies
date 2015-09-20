package com.example.dc.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by DS on 8/1/2015.
 */
public class Film implements Parcelable{

    String mFilmId;
    String mTitle;
    String mOverview;
    String mVotesAverage;
    String mReleaseDate;
    String mImageUrl;
    List<Trailer> mTrailers;
    List<Review> mReviews;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(mFilmId);
        p.writeString(mTitle);
        p.writeString(mOverview);
        p.writeString(mVotesAverage);
        p.writeString(mReleaseDate);
        p.writeString(mImageUrl);
    }

    private Film(Parcel p){
        mFilmId = p.readString();
        mTitle = p.readString();
        mOverview = p.readString();
        mVotesAverage = p.readString();
        mReleaseDate = p.readString();
        mImageUrl = p.readString();
    }

    public static final Parcelable.Creator<Film> CREATOR
            = new Parcelable.Creator<Film>(){
        public Film createFromParcel(Parcel p){
            return new Film(p);
        }
        public Film[] newArray(int size){
            return new Film[size];
        }
    };

    public Film(String id, String title, String overview, String voteAverage, String releaseDate, String imageUrl){

        this.mFilmId = id;
        this.mTitle = title;
        this.mOverview = overview;
        this.mVotesAverage = voteAverage;
        this.mReleaseDate = releaseDate;
        this.mImageUrl = imageUrl;
    }

}
