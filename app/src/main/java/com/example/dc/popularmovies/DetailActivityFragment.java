package com.example.dc.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment
    implements  OnTaskCompleted{

    private Film mFilm;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();


        if(intent != null && intent.hasExtra("bundle")){

            Bundle b = intent.getBundleExtra("bundle");
            this.mFilm = b.getParcelable("film");
            ((TextView)rootView.findViewById(R.id.film_title_text)).setText(mFilm.mTitle);
            ((TextView)rootView.findViewById(R.id.overview_text)).setText(mFilm.mOverview);
            ((TextView)rootView.findViewById(R.id.rating_text)).setText(mFilm.mVotesAverage);

            Picasso.with(getActivity())
                    .load(mFilm.mImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(((ImageView) rootView.findViewById(R.id.film_poster_thumbnail)));

            Log.v("URL", mFilm.mImageUrl);
            Log.v("Release Date", mFilm.mReleaseDate);

            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(mFilm.mReleaseDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                ((TextView)rootView.findViewById(R.id.release_year_text)).setText(Integer.toString(year));
                Log.v("Release Year: ", Integer.toString(year));
            } catch (ParseException e) {
                Log.v("Release Year: ", "FAIL");
                e.printStackTrace();
            }
            if(isNetworkAvailable()){
                FetchDetailsTask fetchDetailsTask = new FetchDetailsTask();
                fetchDetailsTask.execute(mFilm);
            }else{
                Toast.makeText(getActivity(), "No Network Connection Available", Toast.LENGTH_SHORT).show();
            }
        }
        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onTaskCompleted(Film film) {
        mFilm = film;
        View view = getView();
        TextView textView = (TextView) view.findViewById(R.id.runtime_text);
        textView.setText(film.mRunTime);

    }

    public class FetchDetailsTask extends AsyncTask<Film, Void, Film>{

        private final String LOG_TAG = FetchDetailsTask.class.getSimpleName();

        @Override
        protected Film doInBackground(Film... params) {

            if(params.length == 0){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String filmJsonStr = null;
            final String API_KEY= Constants.API_KEY;
            Film film = params[0];

            try{
                final String MDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MDB_BASE_URL).buildUpon()
                        .appendPath(film.mFilmId)
                        .appendQueryParameter(API_KEY_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v("Detail URL", builtUri.toString());

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

                filmJsonStr = buffer.toString();
                Log.v("Detail JSON", filmJsonStr);
            }catch (IOException e){

                Log.e(LOG_TAG, "Error", e);
                return null;

            }finally{
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("DetailActivityFragment", "Error closing stream", e);
                    }
                }

            }

            try{
                return getFilmDetailFromJson(film ,filmJsonStr);
            }catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        private Film getFilmDetailFromJson(Film film, String filmJsonStr)
            throws  JSONException{

            final String MDB_RUNTIME = "runtime";

            JSONObject filmJson = new JSONObject(filmJsonStr);
            String runtime = filmJson.getString(MDB_RUNTIME);
            Log.v("Film Runtime", runtime);
            if (runtime.equals("null"))
                runtime = "Not Available";
            film.mRunTime = runtime;
            return film;
        }


        @Override
        protected void onPostExecute(Film film){
            if (film != null){
                onTaskCompleted(film);
            }
        }
    }
}
