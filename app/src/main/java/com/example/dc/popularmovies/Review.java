package com.example.dc.popularmovies;

/**
 * Created by DS on 9/19/2015.
 */
public class Review {
    String mId;
    String mAuthor;
    String mContent;

    public Review(String id, String author, String content){
        this.mId = id;
        this.mAuthor = author;
        this.mContent = content;
    }
}
