package com.example.dc.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.dc.popularmovies.data.FilmContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class PosterFragment extends Fragment{


    private PosterAdapter mPosterAdapter;
    private List<Film> mFilmList;
    private String mSortOrder;
    private String mRecentOrder;
    private int mPosition;

    private GridView mGridView;

    private static final String SELECTED_KEY = "selected_position";

    private static final String[] FILM_COLUMNS = {
            FilmContract.FilmEntry._ID,
            FilmContract.FilmEntry.COLUMN_FILM_ID,
            FilmContract.FilmEntry.COLUMN_FILM_TITLE,
            FilmContract.FilmEntry.COLUMN_OVERVIEW,
            FilmContract.FilmEntry.COLUMN_RATING,
            FilmContract.FilmEntry.COLUMN_RELEASE,
            FilmContract.FilmEntry.COLUMN_POSTER_PATH
    };

    static final int COL_FILM_ID = 0;
    static final int COL_FILM_IMDB_ID = 1;
    static final int COL_FILM_TITLE = 2;
    static final int COL_FILM_OVERVIEW = 3;
    static final int COL_FILM_RATING = 4;
    static final int COL_FILM_RELEASE = 5;
    static final int COL_FILM_POSTER_PATH = 6;

    public interface Callback {
        public void onItemSelected(Bundle b);
    }

    public PosterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("saved instance state", Boolean.toString((savedInstanceState != null)));
        if (savedInstanceState != null){
            mFilmList = savedInstanceState.getParcelableArrayList("films");
            mRecentOrder = savedInstanceState.getString("order");
        }else{
            mFilmList = new ArrayList<Film>();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mRecentOrder = pref.getString("order", "popularity.desc");
        }

    }

    private void loadFilms(){
        mFilmList.clear();
        boolean hasNetworkConnection = checkNetwork();
        Log.v("loadFilms", mSortOrder + " " + getString(R.string.pref_sort_favorites));
        if(mSortOrder.equals(getString(R.string.pref_sort_favorites))){
            Cursor filmCursor = getActivity().getContentResolver().query(
                    FilmContract.FilmEntry.CONTENT_URI,
                    FILM_COLUMNS,
                    null,
                    null,
                    null
            );

            if(filmCursor != null){
                Log.v("Cursor", filmCursor.toString());
                List<Film> filmList = new ArrayList<Film>();
                while (filmCursor.moveToNext()) {

                    Log.v("Film", Boolean.toString(filmList.add(new Film(
                                    filmCursor.getString(COL_FILM_IMDB_ID),
                                    filmCursor.getString(COL_FILM_TITLE),
                                    filmCursor.getString(COL_FILM_OVERVIEW),
                                    filmCursor.getString(COL_FILM_RATING),
                                    filmCursor.getString(COL_FILM_RELEASE),
                                    filmCursor.getString(COL_FILM_POSTER_PATH)
                            ))));
                    Log.v("Filmlist Size while", Integer.toString(mFilmList.size()));

                }
                filmCursor.close();
                mPosterAdapter.clear();
                Log.v("Filmlist Size", Integer.toString(filmList.size()));
                mPosterAdapter.addAll(filmList);
                Log.v("PosterAdapter", mPosterAdapter.toString());
            }
            else{
                Toast.makeText(getActivity(), getString(R.string.no_favorites), Toast.LENGTH_SHORT).show();
            }
        }
        else if(hasNetworkConnection) {
            FetchFilmsTask filmsTask = new FetchFilmsTask();
            filmsTask.execute(mSortOrder);
        }
    }

    private boolean checkNetwork(){
        if(!Utility.isNetworkAvailable(getActivity())){
            Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void preserveSelection(){
        if (mPosition != GridView.INVALID_POSITION){
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSortOrder = pref.getString("order", "popularity.desc");
        Log.v("sort order", Boolean.toString(mSortOrder.equals(mRecentOrder)));
        Log.v("sort order value - r-s", mRecentOrder + " " + mSortOrder);
        if(mFilmList.isEmpty() || !mSortOrder.equals(mRecentOrder)) {
            loadFilms();
            mRecentOrder = mSortOrder;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPosterAdapter = new PosterAdapter(
                getActivity(),
                mFilmList);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        mGridView.setDrawSelectorOnTop(true);
        mGridView.setAdapter(mPosterAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Film film = mPosterAdapter.getItem(position);
                Bundle b = new Bundle();
                b.putParcelable("film", film);
                ((Callback) getActivity())
                        .onItemSelected(b);
                mPosition = position;
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("films",(ArrayList<? extends Parcelable>) mFilmList);
        savedInstanceState.putString("order", mSortOrder);
        if(mPosition != GridView.INVALID_POSITION){
            savedInstanceState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public class FetchFilmsTask extends AsyncTask<String, Void, List<Film>>{

        private final String LOG_TAG = FetchFilmsTask.class.getSimpleName();

        @Override
        protected List<Film> doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String filmsJsonStr = null;
            final String API_KEY= Constants.API_KEY;

            try{
                final String MDB_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String API_KEY_PARAM = "api_key";
                final String SORT_ORDER_PARAM = "sort_by";

                Uri builtUri = Uri.parse(MDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_ORDER_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, API_KEY )
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v("URL", builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                filmsJsonStr = buffer.toString();
                Log.v("FilmJSON", filmsJsonStr);
            }catch(IOException e){
                Log.e(LOG_TAG, "Error", e);

                return null;
            }finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    }catch(final IOException e){
                        Log.e("PosterFragment", "Error closing stream", e);
                    }
                }
            }

            try{
                return getFilmsFromJson(filmsJsonStr);
            }catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            return null;
        }

        private List<Film> getFilmsFromJson(String filmsJsonStr)
                throws JSONException{

            final String MDB_RESULTS = "results";
            final String MDB_TITLE = "original_title";
            final String MDB_OVERVIEW = "overview";
            final String MDB_VOTE_AVG = "vote_average";
            final String MDB_RELEASE_DATE = "release_date";
            final String MDB_POSTER = "poster_path";
            final String MDB_ID = "id";

            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185";


            JSONObject filmsJson = new JSONObject(filmsJsonStr);
            JSONArray filmArray = filmsJson.getJSONArray(MDB_RESULTS);


            List<Film> filmList = new ArrayList<Film>();

            for(int i =0; i < filmArray.length(); i++){
                JSONObject filmJson = filmArray.getJSONObject(i);
                String posterUrl = POSTER_BASE_URL + filmJson.getString(MDB_POSTER);
                Log.v("Image URL", posterUrl);
                filmList.add(new Film(
                        checkDetailAvailability(filmJson.getString(MDB_ID)),
                        checkDetailAvailability(filmJson.getString(MDB_TITLE)),
                        checkDetailAvailability(filmJson.getString(MDB_OVERVIEW)),
                        checkDetailAvailability(filmJson.getString(MDB_VOTE_AVG)),
                        checkDetailAvailability(filmJson.getString(MDB_RELEASE_DATE)),
                        posterUrl
                ));
            }
            return filmList;
        }

        private String checkDetailAvailability(String filmDetail){

            if (filmDetail.equals("null")){
                filmDetail = "Not Available";
            }
            return filmDetail;
        }

        @Override
        protected void onPostExecute(List<Film> filmList) {
            if(filmList != null){
                mPosterAdapter.clear();
                mPosterAdapter.addAll(filmList);
                preserveSelection();
            }

        }
    }
}
