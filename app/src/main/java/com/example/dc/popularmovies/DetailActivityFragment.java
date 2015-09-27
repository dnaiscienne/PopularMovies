package com.example.dc.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private TextView mTrailerLabelView;
    private TextView mReviewLabelView;

    private LinearLayout mTrailersView;
    private LinearLayout mReviewsView;

    private LayoutInflater mInflater;



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

        mTrailerLabelView = (TextView) rootView.findViewById(R.id.trailer_label);
        mReviewLabelView = (TextView) rootView.findViewById(R.id.review_label);

        mTrailersView = (LinearLayout) rootView.findViewById(R.id.film_trailer_list);
        mReviewsView = (LinearLayout) rootView.findViewById(R.id.film_review_list);

        mInflater = inflater;

        Bundle arguments = getArguments();
        if(arguments != null){
            this.mFilm = arguments.getParcelable("film");
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
                    if(mIsFavorite){
                        removeFromFavorites();
                        Toast.makeText(getActivity(), getString(R.string.removed_favorites), Toast.LENGTH_SHORT).show();
                        mFavoriteButton.setText(R.string.favorite_mark_button_label);
                    }
                    else{
                        insertIntoFavorites();
                        Toast.makeText(getActivity(), getString(R.string.added_favorites), Toast.LENGTH_SHORT).show();
                        mFavoriteButton.setText(R.string.favorite_unmark_button_label);
                    }
                    checkIfFavorite();
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
        if(mFirstTrailer != null){
            mShareActionProvider.setShareIntent(createShareTrailerIntent());

        }

    }


    @Override
    public void onFetchDetailsTaskCompleted(Film film) {
        mFilm = film;
        showFilmDetails();
//        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        showTrailers(mInflater);
        showReviews(mInflater);

    }
    private void showFilmDetails(){
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
    }
    private void showTrailers(LayoutInflater inflater){
        List trailers = mFilm.mTrailers;
        if (trailers != null && !trailers.isEmpty()){
            mTrailerLabelView.setText(getString(R.string.detail_trailer_label));
            mFirstTrailer = createYoutubeUrl(((Trailer)trailers.get(0)).mSource).toString();
            for(final Trailer t : mFilm.mTrailers ){
                View trailerItemView = inflater.inflate(R.layout.list_item_trailer, null, false);
                TextView trailerItemNameView = (TextView)trailerItemView.findViewById(R.id.list_item_trailer_name_textview);
                trailerItemNameView.setText(t.mName);
                final Uri youtubeUrl = createYoutubeUrl(t.mSource);
                trailerItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playTrailer(youtubeUrl);
                    }
                });
                mTrailersView.addView(trailerItemView);
            }
            if(mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareTrailerIntent());
            }
        }
    }

    private void showReviews(LayoutInflater inflater){
        List reviews = mFilm.mReviews;
        if (reviews != null && !reviews.isEmpty()){
            mReviewLabelView.setText(getString(R.string.detail_review_label));
            for(Review r : mFilm.mReviews ){
                View reviewItemView = inflater.inflate(R.layout.list_item_review, null, false);
                TextView reviewItemContentView = (TextView)reviewItemView.findViewById(R.id.list_item_review_content_textview);
                TextView reviewItemAuthorView = (TextView)reviewItemView.findViewById(R.id.list_item_review_author_textview);
                reviewItemContentView.setText(r.mContent);
                reviewItemAuthorView.setText(r.mAuthor);
                mReviewsView.addView(reviewItemView);
            }
        }
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
        if((mFilm.mTrailers != null && !mFilm.mTrailers.isEmpty()) || (mFilm.mReviews != null && !mFilm.mReviews.isEmpty())){
//            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            showFilmDetails();
            showTrailers(mInflater);
            showReviews(mInflater);
        }
        else if(Utility.isNetworkAvailable(getActivity())){
            FetchDetailsTask fetchDetailsTask = new FetchDetailsTask(this);
            fetchDetailsTask.execute(film);
        }else{
            showFilmDetails();
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

    private void checkIfFavorite() {
        if (mFilm != null) {
            Uri filmUri = FilmContract.FilmEntry.buildFilmIdUri(Integer.parseInt(mFilm.mFilmId));
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
        String selectionClause = FilmContract.FilmEntry.COLUMN_FILM_ID + " = ?";
        String[] selectionArgs = {mFilm.mFilmId};
        getActivity().getContentResolver().delete(
                FilmContract.FilmEntry.CONTENT_URI,
                selectionClause,
                selectionArgs
        );
    }
}
