package com.example.dc.popularmovies;

import android.content.Intent;
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
public class DetailActivityFragment extends Fragment {

    Film film;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();


        if(intent != null && intent.hasExtra("bundle")){

            Bundle b = intent.getBundleExtra("bundle");
            this.film = b.getParcelable("film");
            ((TextView)rootView.findViewById(R.id.film_title_text)).setText(film.mTitle);
            ((TextView)rootView.findViewById(R.id.overview_text)).setText(film.mOverview);
            ((TextView)rootView.findViewById(R.id.rating_text)).setText(film.mVotesAverage);

            Picasso.with(getActivity())
                    .load(film.mImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(((ImageView) rootView.findViewById(R.id.film_poster_thumbnail)));

            Log.v("URL", film.mImageUrl);
            Log.v("Release Date", film.mReleaseDate);

            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(film.mReleaseDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int year = calendar.get(Calendar.YEAR);
                ((TextView)rootView.findViewById(R.id.release_year_text)).setText(Integer.toString(year));
                Log.v("Release Year: ", Integer.toString(year));
            } catch (ParseException e) {
                Log.v("Release Year: ", "FAIL");
                e.printStackTrace();
            }
            FetchDetailsTask fetchDetailsTask = new FetchDetailsTask();
            fetchDetailsTask.execute(film.mFilmId);
        }
        return rootView;
    }
    public class FetchDetailsTask extends AsyncTask<String, Void, String>{

        private final String LOG_TAG = FetchDetailsTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String filmJsonStr = null;
            final String API_KEY= Constants.API_KEY;

            try{
                final String MDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MDB_BASE_URL).buildUpon()
                        .appendPath(params[0])
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
            }catch (Exception e){

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
                return getFilmDetailFromJson(filmJsonStr);
            }catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        private String getFilmDetailFromJson(String filmJsonStr)
            throws  JSONException{

            final String MDB_RUNTIME = "runtime";

            JSONObject filmJson = new JSONObject(filmJsonStr);
            return filmJson.getString(MDB_RUNTIME);
        }


        @Override
        protected void onPostExecute(String filmDetail) {
            String text;
            if(filmDetail.equals("null")){
                text = "Not Available";
            }else{
                text = String.format(getResources().getString(R.string.film_runtime), filmDetail);
            }
            TextView textView = (TextView) getActivity().findViewById(R.id.runtime_text);
            textView.setText(text);
        }
    }
}
