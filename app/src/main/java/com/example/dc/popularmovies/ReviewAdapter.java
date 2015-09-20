package com.example.dc.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by DS on 9/20/2015.
 */
public class ReviewAdapter extends ArrayAdapter<Review> {
    private static final String LOG_TAG = Trailer.class.getSimpleName();

    public ReviewAdapter(Activity context, List<Review> reviews){
        super(context, 0, reviews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Review review = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_review, parent, false);
        }
        TextView reviewNameView = (TextView)convertView;
        reviewNameView.setText(review.mContent);
        return reviewNameView;
    }

}
