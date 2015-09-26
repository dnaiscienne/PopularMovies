package com.example.dc.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DS on 9/19/2015.
 */
public class Review  implements Parcelable{
    String mId;
    String mAuthor;
    String mContent;

    public Review(String id, String author, String content){
        this.mId = id;
        this.mAuthor = author;
        this.mContent = content;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(mId);
        p.writeString(mAuthor);
        p.writeString(mContent);
    }

    private Review(Parcel p){
        mId = p.readString();
        mAuthor = p.readString();
        mContent = p.readString();
    }

    public static final Parcelable.Creator<Review> CREATOR
            = new Parcelable.Creator<Review>(){
        public Review createFromParcel(Parcel p){
            return new Review(p);
        }
        public Review[] newArray(int size){
            return new Review[size];
        }
    };
}
