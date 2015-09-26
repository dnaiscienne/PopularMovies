package com.example.dc.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DS on 9/19/2015.
 */
public class Trailer implements Parcelable{
    String mName;
    String mSource;

    public Trailer(String name, String source) {
        this.mName = name;
        this.mSource = source;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(mName);
        p.writeString(mSource);
    }

    private Trailer(Parcel p){
        mName = p.readString();
        mSource = p.readString();
    }

    public static final Parcelable.Creator<Trailer> CREATOR
            = new Parcelable.Creator<Trailer>(){
        public Trailer createFromParcel(Parcel p){
            return new Trailer(p);
        }
        public Trailer[] newArray(int size){
            return new Trailer[size];
        }
    };
}
