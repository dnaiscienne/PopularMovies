package com.example.dc.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by DS on 9/19/2015.
 */
public class TrailerAdapter extends ArrayAdapter<Trailer> {

    private static final String LOG_TAG = Trailer.class.getSimpleName();

    public TrailerAdapter(Activity context, List<Trailer> trailers){
        super(context, 0, trailers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trailer trailer = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_trailer, parent, false);
        }
        TextView trailerNameView = (TextView)convertView;
        trailerNameView.setText(trailer.mName);
        return trailerNameView;
    }
}
