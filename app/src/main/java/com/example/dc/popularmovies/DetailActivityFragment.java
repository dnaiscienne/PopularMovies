package com.example.dc.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dc.popularmovies.data.FilmContract;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment
        implements FetchDetailsTask.OnTaskCompleted{

    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private ShareActionProvider mShareActionProvider;

    private Film mFilm;
    private String mFirstTrailer;
    private boolean mIsFavorite;

    private TextView mTitleView;
    private TextView mOverviewView;
    private TextView mRatingView;
    private TextView mRuntimeView;
    private TextView mReleaseView;
    private ImageView mPosterView;
    private Button mFavoriteButton;

    private LinearLayout mTrailersView;
    private LinearLayout mReviewsView;

//    private TextView mTrailerItemView;
    private TextView mReviewItemView;

//    private ListView mTrailersListView;
//    private ListView mReviewsListView;

//    private ListAdapter mTrailerAdapter;
//    private ListAdapter mReviewAdapter;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitleView = (TextView)rootView.findViewById(R.id.film_title_text);
        mOverviewView = (TextView)rootView.findViewById(R.id.overview_text);
        mRatingView = (TextView)rootView.findViewById(R.id.rating_text);
        mRuntimeView = (TextView)rootView.findViewById(R.id.runtime_text);
        mReleaseView = (TextView)rootView.findViewById(R.id.release_year_text);
        mPosterView = (ImageView) rootView.findViewById(R.id.film_poster_thumbnail);
        mFavoriteButton = (Button) rootView.findViewById(R.id.favorite_button);

        mTrailersView = (LinearLayout) rootView.findViewById(R.id.film_trailer_list);
        mReviewsView = (LinearLayout) rootView.findViewById(R.id.film_review_list);

//        mTrailersListView = (ListView) rootView.findViewById(R.id.film_trailer_list);
//        mReviewsListView = (ListView) rootView.findViewById(R.id.film_review_list);

        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra("bundle")) {
            Bundle b = intent.getBundleExtra("bundle");
            this.mFilm = b.getParcelable("film");
        }
        if(mFilm != null){
            //Fetch additional details
            fetchFilmDetails(mFilm);
            checkIfFavorite();

            if(mIsFavorite){
                mFavoriteButton.setText(R.string.favorite_unmark_button_label);
            }

            //Mark as favorite
            mFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Favorite", Toast.LENGTH_SHORT).show();

                    if(mIsFavorite){
                        removeFromFavorites();
                        mFavoriteButton.setText(R.string.favorite_mark_button_label);
                    }
                    else{
                        insertIntoFavorites();
                        mFavoriteButton.setText(R.string.favorite_unmark_button_label);
                    }







//                    ContentValues filmValues = new ContentValues();
//                    filmValues.put(FilmContract.FilmEntry.COLUMN_FILM_ID, mFilm.mFilmId);
//                    filmValues.put(FilmContract.FilmEntry.COLUMN_FILM_TITLE, mFilm.mTitle);
//                    filmValues.put(FilmContract.FilmEntry.COLUMN_OVERVIEW, mFilm.mOverview);
//                    filmValues.put(FilmContract.FilmEntry.COLUMN_RATING, mFilm.mVotesAverage);
//                    filmValues.put(FilmContract.FilmEntry.COLUMN_RELEASE, mFilm.mReleaseDate);
//                    filmValues.put(FilmContract.FilmEntry.COLUMN_POSTER_PATH, mFilm.mImageUrl);
//
//                    Uri uri = getActivity().getContentResolver().insert(FilmContract.FilmEntry.CONTENT_URI, filmValues);
//
//                    Uri filmUri = FilmContract.FilmEntry.buildFilmIdUri(Integer.parseInt(mFilm.mFilmId));
//
//                    //check if in favorite table
//                    Cursor c = getActivity().getContentResolver().query(
//                            filmUri,
//                            null,
//                            null,
//                            null,
//                            null
//                    );
//
//                    if(c.moveToFirst()){
//                        Toast.makeText(getActivity(), mFilm.mFilmId, Toast.LENGTH_SHORT).show();
//                    }
//
//                    c.close();

                }
            });
        }
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            mFilm = savedInstanceState.getParcelable("film");
            checkIfFavorite();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("film", mFilm);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain").setText("hi").getIntent();
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

//        mShareActionProvider.setShareIntent(createShareTrailerIntent());
    }


    @Override
    public void onFetchDetailsTaskCompleted(Film film) {
        mFilm = film;
        mTitleView.setText(mFilm.mTitle);
        mOverviewView.setText(mFilm.mOverview);
        mRatingView.setText(mFilm.mVotesAverage);

        Picasso.with(getActivity())
                .load(mFilm.mImageUrl)
                .placeholder(R.drawable.placeholder)
                .into(mPosterView);

        Log.v("URL", mFilm.mImageUrl);
        Log.v("Release Date", mFilm.mReleaseDate);



        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(mFilm.mReleaseDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            mReleaseView.setText(Integer.toString(year));
            Log.v("Release Year: ", Integer.toString(year));
        } catch (ParseException e) {
            Log.v("Release Year: ", "FAIL");
            e.printStackTrace();
        }

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        List trailers = mFilm.mTrailers;
        if (trailers != null && !trailers.isEmpty()){
            mFirstTrailer = createYoutubeUrl(((Trailer)trailers.get(0)).mSource).toString();
            for(final Trailer t : mFilm.mTrailers ){
                View trailerItemView = inflater.inflate(R.layout.list_item_trailer, null, false);
//                mTrailerItemView = (TextView) inflater.inflate(R.layout.list_item_trailer, null, false);
                TextView trailerItemNameView = (TextView)trailerItemView.findViewById(R.id.list_item_trailer_name_textview);
                trailerItemNameView.setText(t.mName);
                final Uri youtubeUrl = createYoutubeUrl(t.mSource);
                trailerItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(getActivity(), t.mName, Toast.LENGTH_SHORT).show();
                        playTrailer(youtubeUrl);
                    }
                });
                mTrailersView.addView(trailerItemView);
            }
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }

        List reviews = mFilm.mReviews;
        if (reviews != null && !reviews.isEmpty()){
            for(Review r : mFilm.mReviews ){
                mReviewItemView = (TextView) inflater.inflate(R.layout.list_item_review, null, false);
                mReviewItemView.setText(r.mContent);
                mReviewsView.addView(mReviewItemView);
            }
        }


//        mTrailerAdapter = new TrailerAdapter(getActivity(),mFilm.mTrailers);
//        mTrailersListView.setAdapter(mTrailerAdapter);

//        mReviewAdapter = new ReviewAdapter(getActivity(), mFilm.mReviews);
//        mReviewsListView.setAdapter(mReviewAdapter);

//        Utility.setDynamicHeight(mTrailersListView);
//        Utility.setDynamicHeight(mReviewsListView);
    }
    private Uri createYoutubeUrl(String trailerSource){
        final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch";
        final String YOUTUBE_VIDEO_PARAM = "v";
        Uri youtubeUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_VIDEO_PARAM, trailerSource).build();
        return youtubeUri;
    }
    private void playTrailer(Uri youtubeUri){
        Intent intent = new Intent(Intent.ACTION_VIEW, youtubeUri);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't open " + youtubeUri + ", no receiving apps installed!");
        }
    }
    private void fetchFilmDetails(Film film){
        if(Utility.isNetworkAvailable(getActivity())){
            FetchDetailsTask fetchDetailsTask = new FetchDetailsTask(this);
            fetchDetailsTask.execute(film);
        }else{
            Toast.makeText(getActivity(), "No Network Connection Available", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent createShareTrailerIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFirstTrailer);
        return shareIntent;
    }

    private void checkIfFavorite(){
        Uri filmUri = FilmContract.FilmEntry.buildFilmIdUri(Integer.parseInt(mFilm.mFilmId));

        //check if in favorites table
        Cursor c = getActivity().getContentResolver().query(
                filmUri,
                null,
                null,
                null,
                null
        );
        mIsFavorite = c.moveToFirst();
        c.close();
    }
    private void insertIntoFavorites(){
        ContentValues filmValues = new ContentValues();
        filmValues.put(FilmContract.FilmEntry.COLUMN_FILM_ID, mFilm.mFilmId);
        filmValues.put(FilmContract.FilmEntry.COLUMN_FILM_TITLE, mFilm.mTitle);
        filmValues.put(FilmContract.FilmEntry.COLUMN_OVERVIEW, mFilm.mOverview);
        filmValues.put(FilmContract.FilmEntry.COLUMN_RATING, mFilm.mVotesAverage);
        filmValues.put(FilmContract.FilmEntry.COLUMN_RELEASE, mFilm.mReleaseDate);
        filmValues.put(FilmContract.FilmEntry.COLUMN_POSTER_PATH, mFilm.mImageUrl);

        getActivity().getContentResolver().insert(FilmContract.FilmEntry.CONTENT_URI, filmValues);
    }
    private void removeFromFavorites(){
        int rowsDeleted = 0;
    }
}
