package com.example.dc.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by D.C on 7/31/2015.
 */
public class PosterAdapter extends ArrayAdapter<Film> {

    private static final String LOG_TAG = PosterAdapter.class.getSimpleName();

    public PosterAdapter(Activity context, List<Film> films){

        super(context, 0, films);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Film film = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.poster_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView;
        Picasso.with(getContext())
                .load(film.mImageUrl)
                .placeholder(R.drawable.placeholder)
                .into(imageView);

        return imageView;
    }

}
