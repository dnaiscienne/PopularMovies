package com.example.dc.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment
        implements FetchDetailsTask.OnTaskCompleted{

    private Film mFilm;

    private TextView mTitleView;
    private TextView mOverviewView;
    private TextView mRatingView;
    private TextView mRuntimeView;
    private TextView mReleaseView;
    private ImageView mPosterView;
    private ListView mTrailersView;

    private ListAdapter mTrailerAdapter;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//        mTitleView = (TextView)rootView.findViewById(R.id.film_title_text);
//        mOverviewView = (TextView)rootView.findViewById(R.id.overview_text);
//        mRatingView = (TextView)rootView.findViewById(R.id.rating_text);
//        mRuntimeView = (TextView)rootView.findViewById(R.id.runtime_text);
//        mReleaseView = (TextView)rootView.findViewById(R.id.release_year_text);
//        mPosterView = (ImageView) rootView.findViewById(R.id.film_poster_thumbnail);
        mTrailersView = (ListView) rootView.findViewById(R.id.trailer_list);

        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra("bundle")) {
            Bundle b = intent.getBundleExtra("bundle");
            this.mFilm = b.getParcelable("film");
        }
        if(mFilm != null){
            fetchFilmDetails(mFilm);
        }
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            mFilm = savedInstanceState.getParcelable("film");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("film", mFilm);
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onFetchDetailsTaskCompleted(Film film) {
//        mFilm = film;
//        mTitleView.setText(mFilm.mTitle);
//        mOverviewView.setText(mFilm.mOverview);
//        mRatingView.setText(mFilm.mVotesAverage);
//        mRuntimeView.setText(mFilm.mRunTime);
//
//        Picasso.with(getActivity())
//                .load(mFilm.mImageUrl)
//                .placeholder(R.drawable.placeholder)
//                .into(mPosterView);

        Log.v("URL", mFilm.mImageUrl);
        Log.v("Release Date", mFilm.mReleaseDate);



//        try {
//            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(mFilm.mReleaseDate);
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(date);
//            int year = calendar.get(Calendar.YEAR);
//            mReleaseView.setText(Integer.toString(year));
//            Log.v("Release Year: ", Integer.toString(year));
//        } catch (ParseException e) {
//            Log.v("Release Year: ", "FAIL");
//            e.printStackTrace();
//        }

        mTrailerAdapter = new TrailerAdapter(getActivity(),mFilm.mTrailers);
        mTrailersView.setAdapter(mTrailerAdapter);
    }
    private void fetchFilmDetails(Film film){
        if(Utility.isNetworkAvailable(getActivity())){
            FetchDetailsTask fetchDetailsTask = new FetchDetailsTask(this);
            fetchDetailsTask.execute(film);
        }else{
            Toast.makeText(getActivity(), "No Network Connection Available", Toast.LENGTH_SHORT).show();
        }
    }
}
