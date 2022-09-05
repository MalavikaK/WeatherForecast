package com.example.hw03;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.Connection;

public class CityWeatherActivity extends AppCompatActivity implements WeatherAdapter.OnWeatherItemClickListener{

    TextView tv_cityCountry;
    TextView tv_forecastDate;
    TextView tv_headline;
    TextView tv_temp;
    TextView tv_dayPhrase;
    TextView tv_nightPhrase;
    TextView tv_URL;

    ImageView iv_day;
    ImageView iv_night;

    RecyclerView recyclerView;
    RecyclerView.Adapter rv_adapter;

    Button btn_saveCityWeather;
    Button btn_currentCityWeather;

    String forecastURL;
    static String cityName = "";
    static String countryName = "";

    static CityWeatherActivity contextMain;

    ArrayList<ForecastDet> forecastDet = new ArrayList<>();

    int flagInitial = 0;
    int dataPos = 0;

    RecyclerView.LayoutManager rv_layoutManager;


    String text ="";
    String pattern1 = "MMM dd, yyyy";
    String pattern2 = "yyyy-MM-dd";
    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(pattern1);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern2);

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_weather);

        setTitle("City Weather");

        tv_cityCountry = findViewById(R.id.tv_cityCountry);
        tv_forecastDate = findViewById(R.id.tv_forecastDate);
        tv_headline = findViewById(R.id.tv_headline);
        tv_temp = findViewById(R.id.tv_temp);
        tv_dayPhrase = findViewById(R.id.tv_dayPhrase);
        tv_nightPhrase = findViewById(R.id.tv_nightPhrase);
        iv_day = findViewById(R.id.iv_day);
        iv_night = findViewById(R.id.iv_night);
        tv_URL = findViewById(R.id.tv_URL);
        recyclerView = findViewById(R.id.recylerViewWeather);
        btn_saveCityWeather = findViewById(R.id.btn_saveCityWeather);
        btn_currentCityWeather = findViewById(R.id.btn_currentCityWeather);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);
        contextMain = CityWeatherActivity.this;


        recyclerView.setHasFixedSize(true);

        rv_layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(rv_layoutManager);

        btn_currentCityWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = forecastDet.get(dataPos).getKey();
                finish();
                Intent intent = new Intent(CityWeatherActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Key",key);
                startActivity(intent);
            }
        });

        btn_saveCityWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new GetWeatherData().execute(forecastDet.get(dataPos).getKey());

            }
        });

        String json = getIntent().getStringExtra("WEATHER");
        try {
            JSONObject obj = new JSONObject(json);
            cityName = obj.getString("EnglishName");
            countryName = obj.getJSONObject("Country").getString("ID");
            tv_cityCountry.setText(cityName + ", " + countryName);

            new GetForecastJSON().execute(obj.getString("Key"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(int position) {

        setRecyclerViewData(forecastDet.get(position));
        dataPos = position;
        System.out.println("Clicked item "+ position);
    }

    public class GetWeatherData extends AsyncTask<String, Void, String> {

        String date;

        @Override
        protected String doInBackground(String... strings) {


            String currentConditionsURL = "https://dataservice.accuweather.com/currentconditions/v1/*?apikey=" + getResources().getString(R.string.api_key);
            if(strings[0].length() > 0) {
                currentConditionsURL = currentConditionsURL.replace("*", strings[0]);
            }

            HttpURLConnection connection = null;
            try {
                URL url = new URL(currentConditionsURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = IOUtils.toString(connection.getInputStream(), "UTF8");
                    JSONArray jsonArray = new JSONArray(json);
                    JSONObject root = jsonArray.getJSONObject(0);

                    date = root.getString("LocalObservationDateTime");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                if(connection!=null){
                    connection.disconnect();
                }
            }
            return  date;

        }

        @Override
        protected void onPostExecute(String date) {
            super.onPostExecute(date);

            City city = new City();
            city.setCityName(cityName);
            city.setCountry(countryName);
            city.setCityKey(forecastDet.get(dataPos).getKey());
            city.setDate(date);

            int flag = 0;
            int index = forecastDet.get(dataPos).getTemp().indexOf("/");
            String temp = forecastDet.get(dataPos).getTemp();
            temp = temp.substring(0,index);
            city.setTemperature(Double.parseDouble(temp));
            for (int i = 0; i < MainActivity.savedCityList.size(); i++) {
                if (city.getCityName().equals(MainActivity.savedCityList.get(i).getCityName())) {
                    MainActivity.savedCityList.get(i).setTemperature(city.getTemperature());
                    flag = 1;
                }
            }
            if(flag == 0){
                MainActivity.savedCityList.add(city);
            }

            MainActivity.tv_cityDesc.setVisibility(View.INVISIBLE);
            MainActivity.tv_boxDesc.setVisibility(View.INVISIBLE);
            MainActivity.tv_savedCities.setVisibility(View.VISIBLE);

            MainActivity.recyclerView.setVisibility(View.VISIBLE);
            MainActivity.rv_adapter = new MyAdapter(MainActivity.savedCityList, MainActivity.contextMain);
            MainActivity.recyclerView.setAdapter(MainActivity.rv_adapter);

            MainActivity.tv_cityName.setText(null);
            MainActivity.tv_country.setText(null);
            finish();
            Intent intent = new Intent(CityWeatherActivity.this,MainActivity.class);
            if(MainActivity.tv_CityCountry.getText().toString().length() > 0){
                intent.putExtra("CityCountry", MainActivity.tv_CityCountry.getText().toString());
                intent.putExtra("Temp", MainActivity.tv_temp.getText().toString());
                intent.putExtra("Updated", MainActivity.tv_updated.getText().toString());
                intent.putExtra("WeatherText", MainActivity.tv_weatherText.getText().toString());
                MainActivity.iv_weatherIcon.buildDrawingCache();
                Bitmap image= MainActivity.iv_weatherIcon.getDrawingCache();
                Bundle extras = new Bundle();
                extras.putParcelable("imagebitmap", image);
                intent.putExtra("Image", extras);
            }
            intent.putExtra("FLAG",flag);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    }

    public class GetForecastJSON extends AsyncTask<String, Void, ArrayList<ForecastDet>> {

        @Override
        protected ArrayList<ForecastDet> doInBackground(String... strings) {


            HttpURLConnection connection = null;

            forecastURL = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/"+ strings[0] +"?apikey=" + getResources().getString(R.string.api_key);

            try {
                URL url = new URL(forecastURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = IOUtils.toString(connection.getInputStream(), "UTF8");
                    JSONObject jsonObject = new JSONObject(json);

                    text = jsonObject.getJSONObject("Headline").getString("Text");
                    JSONArray dailyForecast = jsonObject.getJSONArray("DailyForecasts");

                    JSONArray sortedForecast = new JSONArray();

                    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
                    for (int i = 0; i < dailyForecast.length(); i++) {
                        jsonValues.add(dailyForecast.getJSONObject(i));
                    }
                    Collections.sort( jsonValues, new Comparator<JSONObject>() {
                        //You can change "Name" with "ID" if you want to sort by ID
                        private static final String KEY_NAME = "Date";

                        @Override
                        public int compare(JSONObject a, JSONObject b) {
                            String valA = new String();
                            String valB = new String();

                            try {
                                valA = (String) a.get(KEY_NAME);
                                valB = (String) b.get(KEY_NAME);
                            }
                            catch (JSONException e) {
                                //do something
                            }

                            return valA.compareTo(valB);
                            //if you want to change the sort order, simply use the following:
                            //return -valA.compareTo(valB);
                        }
                    });

                    for (int i = 0; i < dailyForecast.length(); i++) {
                        sortedForecast.put(jsonValues.get(i));
                    }

                    for (int i = 0; i < sortedForecast.length(); i++) {
                        JSONObject currentForecast = sortedForecast.getJSONObject(i);
                        String currentDate = currentForecast.getString("Date");
                        String dateNew = "";

                        String dateSet = currentDate.split("T")[0].trim();
                        try {
                            Date date = simpleDateFormat.parse(dateSet);
                            dateNew = simpleDateFormat1.format(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String temp = currentForecast.getJSONObject("Temperature").getJSONObject("Maximum").getString("Value")+ "/" +
                                currentForecast.getJSONObject("Temperature").getJSONObject("Minimum").getString("Value") + "F";

                        String dayIcon = currentForecast.getJSONObject("Day").getString("Icon");
                        String dayIconPhrase = currentForecast.getJSONObject("Day").getString("IconPhrase");

                        if(10 > Integer.parseInt(dayIcon)){
                            dayIcon = "0".concat(dayIcon);
                        }

                        String nightIcon = currentForecast.getJSONObject("Night").getString("Icon");
                        String nightIconPhrase = currentForecast.getJSONObject("Night").getString("IconPhrase");

                        if(10 > Integer.parseInt(nightIcon)){
                            nightIcon = "0".concat(nightIcon);
                        }

                        String mobileLink = currentForecast.getString("MobileLink");

                        ForecastDet det = new ForecastDet();
                        det.setKey(strings[0]);
                        det.setDate(dateNew);
                        det.setDayIcon(dayIcon);
                        det.setDayIconPhrase(dayIconPhrase);
                        det.setMobileLink(mobileLink);
                        det.setNightIcon(nightIcon);
                        det.setNightIconPhrase(nightIconPhrase);
                        det.setTemp(temp);
                        forecastDet.add(det);

                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return  forecastDet;

        }

        @Override
        protected void onPostExecute(ArrayList<ForecastDet> forecastDets) {
            super.onPostExecute(forecastDets);
            tv_headline.setText(text);
            if(flagInitial == 0){

                setRecyclerViewData(forecastDets.get(0));

                rv_adapter = new WeatherAdapter(forecastDets, CityWeatherActivity.contextMain);
                recyclerView.setAdapter(rv_adapter);
                rv_adapter.notifyDataSetChanged();

                flagInitial = 1;
            }

            progressBar.setVisibility(View.INVISIBLE);


        }
    }

    public void setRecyclerViewData(final ForecastDet det){

        tv_forecastDate.setText("Forecast on " + det.getDate());
        tv_temp.setText("Temperature "+ det.getTemp());

        String imageURL = "https://developer.accuweather.com/sites/default/files/"+det.getDayIcon()+"-s.png";
        Picasso.get().load(imageURL).into(iv_day);
        imageURL = "https://developer.accuweather.com/sites/default/files/"+det.getNightIcon()+"-s.png";
        Picasso.get().load(imageURL).into(iv_night);

        tv_dayPhrase.setText(det.getDayIconPhrase());
        tv_nightPhrase.setText(det.getNightIconPhrase());

        tv_URL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(det.getMobileLink()));
                startActivity(browserIntent);
            }
        });

    }

}
