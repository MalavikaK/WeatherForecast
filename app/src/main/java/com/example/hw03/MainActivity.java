/**
 * Assignment : Homework 03
 * Group No : 9
 * Name : Aditi Balachandran and Luckose Manuel
 */


package com.example.hw03;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WeatherAlertDialog.AlertDialogListener, MyAdapter.OnItemClickListener{

    static Context context;
    static MainActivity contextMain;
    static Button btn_setCity;
    Button btn_searchCity;

    static TextView tv_cityName;
    static TextView tv_country;
    ListView listView;
    static TextView tv_currentCity;
    static TextView tv_CityCountry;
    static TextView tv_weatherText;
    static TextView tv_temp;
    static TextView tv_updated;
    static TextView tv_cityDesc;
    static TextView tv_boxDesc;
    static TextView tv_savedCities;

    static String currentCity = "";
    static String currentCountry = "";
    static String imageURL = "";

    static ProgressBar progressBar;

    static ImageView iv_weatherIcon;

    static String locationsBaseURL;
    static String currentConditionsURL;

    static String key = "";
    static int flag = 0;

    static ArrayList<City> savedCityList = new ArrayList<>();
    static List<String> listCityCountry = new ArrayList<>();

    static RecyclerView recyclerView;
    static RecyclerView.Adapter rv_adapter;

    static HashMap<Integer, String> cityNames = new HashMap<>();

    ArrayList<JSONObject> jsonObj;

    static int index = 0;
    static int flagButton = 0;

    RecyclerView.LayoutManager rv_layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.context = getApplicationContext();
        contextMain = MainActivity.this;

        setTitle("Weather App");

        btn_setCity = findViewById(R.id.btn_setCity);
        btn_searchCity = findViewById(R.id.btn_searchCity);

        listView = findViewById(R.id.listView);

        tv_weatherText = findViewById(R.id.tv_weatherText);
        tv_currentCity = findViewById(R.id.tv_currentCity);
        tv_cityName = findViewById(R.id.pt_cityName);
        tv_country = findViewById(R.id.pt_Country);
        tv_CityCountry = findViewById(R.id.tv_CityCountry);
        tv_CityCountry.setClickable(true);
        tv_temp = findViewById(R.id.tv_temp);
        tv_updated = findViewById(R.id.tv_updated);
        tv_cityDesc = findViewById(R.id.tv_cityDesc);
        tv_boxDesc = findViewById(R.id.tv_boxDesc);
        tv_savedCities = findViewById(R.id.tv_savedCities);

        iv_weatherIcon = findViewById(R.id.iv_weatherIcon);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        rv_layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rv_layoutManager);

        btn_searchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected()) {
                    jsonObj = new ArrayList<>();
                    listCityCountry = new ArrayList<>();
                    flagButton = 1;
                    new GetCitiesJSON().execute(tv_cityName.getText().toString(),tv_country.getText().toString());
                }else{
                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

        if(null != getIntent().getExtras()) {
            Intent intent = getIntent();
            if(intent.getExtras().containsKey("Key")){
                key = intent.getExtras().getString("Key");
                if(currentCity.equals(CityWeatherActivity.cityName) && currentCountry.equals(CityWeatherActivity.countryName)){
                    Toast.makeText(MainActivity.this, "Current City Updated", Toast.LENGTH_SHORT).show();
                }else{
                    currentCity = CityWeatherActivity.cityName;
                    currentCountry = CityWeatherActivity.countryName;
                    Toast.makeText(MainActivity.this, "Current City Saved", Toast.LENGTH_SHORT).show();
                }
                new MainActivity.GetConditionsJSON().execute();
            }
            else{
                int flag = intent.getExtras().getInt("FLAG");
                if (flag == 1) {
                    Toast.makeText(MainActivity.this, "City Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "City Saved", Toast.LENGTH_SHORT).show();
                }


                if(intent.getExtras().containsKey("CityCountry")) {
                    String CityCountry = intent.getExtras().getString("CityCountry");
                    String Temp = intent.getExtras().getString("Temp");
                    String Updated = intent.getExtras().getString("Updated");
                    String WeatherText = intent.getExtras().getString("WeatherText");

                    Bundle extras = getIntent().getExtras().getParcelable("Image");
                    Bitmap bmp = (Bitmap) extras.getParcelable("imagebitmap");


                    tv_CityCountry.setText(CityCountry);
                    tv_weatherText.setText(WeatherText);
                    tv_temp.setText(Temp);
                    tv_updated.setText(Updated);
                    iv_weatherIcon.setImageBitmap(bmp);

                }




                if(tv_CityCountry.getText().toString().length() >0){

                    tv_currentCity.setVisibility(View.INVISIBLE);
                    btn_setCity.setVisibility(View.INVISIBLE);

                    tv_CityCountry.setVisibility(View.VISIBLE);
                    tv_weatherText.setVisibility(View.VISIBLE);
                    tv_temp.setVisibility(View.VISIBLE);
                    iv_weatherIcon.setVisibility(View.VISIBLE);
                    tv_updated.setVisibility(View.VISIBLE);
                }

                MainActivity.tv_cityDesc.setVisibility(View.INVISIBLE);
                MainActivity.tv_boxDesc.setVisibility(View.INVISIBLE);
                MainActivity.tv_savedCities.setVisibility(View.VISIBLE);

                MainActivity.recyclerView.setVisibility(View.VISIBLE);
                MainActivity.rv_adapter = new MyAdapter(MainActivity.savedCityList, MainActivity.contextMain);
                MainActivity.recyclerView.setAdapter(MainActivity.rv_adapter);



            }
        }


        btn_setCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected()) {
                    openDialog();
                }else{
                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_CityCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected()) {
                    openDialog();
                }else{
                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
        public void openDialog(){

            WeatherAlertDialog builder = new WeatherAlertDialog();
            builder.show(getSupportFragmentManager(), null);

        }

    @Override
    public void returnTexts(String cityName, String country) {
        currentCity = cityName;
        currentCountry = country;
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }        return true;
    }

    @Override
    public void onItemClick(int position) {
        savedCityList.remove(position);
        rv_adapter.notifyDataSetChanged();
        if(savedCityList.size()==0){

            tv_savedCities.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
//            rv_adapter = new MyAdapter(savedCityList, MainActivity.contextMain);
//            recyclerView.setAdapter(rv_adapter);
            recyclerView.setVisibility(View.INVISIBLE);

            tv_cityDesc.setVisibility(View.VISIBLE);
            tv_boxDesc.setVisibility(View.VISIBLE);
        }
    }

    public class GetCitiesJSON extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {
            {
                HttpURLConnection connection = null;
                String[] cityList;

                locationsBaseURL = "https://dataservice.accuweather.com/locations/v1/cities/*/search?apikey=" + context.getResources().getString(R.string.api_key) + "&q=!";

                if(strings[1].length() > 0)
                    locationsBaseURL = locationsBaseURL.replace("*",strings[1]);
                if(strings[0].length() > 0)
                    locationsBaseURL = locationsBaseURL.replace("!",strings[0]);

                try {
                    URL url = new URL(locationsBaseURL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        String json = IOUtils.toString(connection.getInputStream(), "UTF8");
                        JSONArray jsonArray = new JSONArray(json);
                        for(int i =0; i < jsonArray.length();i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String city = obj.getString("EnglishName");
                            String country = obj.getJSONObject("AdministrativeArea").getString("ID");
                            String item = city + ", " + country;
                            listCityCountry.add(item);
                            jsonObj.add(obj);
                        }
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
                if(listCityCountry.size()>0){
                    cityList = new String[listCityCountry.size()];
                    for(int i = 0; i<listCityCountry.size();i++){
                        cityList[i] = listCityCountry.get(i);
                    }
                    return cityList;
                }


                return null;
            }

        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            if(null!=s && s.length >0) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
                dlgAlert.setTitle("Select Cities");
                dlgAlert.setItems(s, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this,CityWeatherActivity.class);
                        intent.putExtra("WEATHER", jsonObj.get(i).toString());
                        startActivity(intent);
                    }
                });
                AlertDialog alertDialog = dlgAlert.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(900, 600);
            }
            else
                Toast.makeText(MainActivity.context, "City not found", Toast.LENGTH_SHORT).show();

        }
    }

    public static class GetKeyJSON extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            {
                HttpURLConnection connection = null;

                locationsBaseURL = "https://dataservice.accuweather.com/locations/v1/cities/*/search?apikey=" + context.getResources().getString(R.string.api_key) + "&q=!";

                if(currentCountry.length() > 0)
                    locationsBaseURL = locationsBaseURL.replace("*",currentCountry);
                if(currentCity.length() > 0)
                    locationsBaseURL = locationsBaseURL.replace("!",currentCity);

                try {
                    URL url = new URL(locationsBaseURL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        String json = IOUtils.toString(connection.getInputStream(), "UTF8");
                        JSONArray jsonArray = new JSONArray(json);
                        if(jsonArray != null && jsonArray.length() > 0) {
                            JSONObject root = jsonArray.getJSONObject(0);
                            key = root.getString("Key");
                        }
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
                return key;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(key.length()>0) {
                Toast.makeText(MainActivity.context, "Current City details saved", Toast.LENGTH_SHORT).show();
                flag++;
            }
            else
                Toast.makeText(MainActivity.context, "City not found", Toast.LENGTH_SHORT).show();

                Log.d("demo", key);
                new MainActivity.GetConditionsJSON().execute();
            if(flag>1) {
//                tv_cityDesc.setVisibility(View.INVISIBLE);
//                tv_boxDesc.setVisibility(View.INVISIBLE);
//
//                tv_savedCities.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.VISIBLE);
//                rv_adapter = new MyAdapter(cityList, MainActivity.contextMain);
//                recyclerView.setAdapter(rv_adapter);
            }
        }
    }

    public static class GetConditionsJSON extends AsyncTask<Void, Void, Conditions>{

        @Override
        protected Conditions doInBackground(Void... voids) {

            HttpURLConnection connection = null;
            Conditions conditions = null;
            currentConditionsURL = "https://dataservice.accuweather.com/currentconditions/v1/*?apikey=" + context.getResources().getString(R.string.api_key);
            if(key.length() > 0) {
                currentConditionsURL = currentConditionsURL.replace("*", key);
            try {
                conditions = new Conditions();
                URL url = new URL(currentConditionsURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = IOUtils.toString(connection.getInputStream(), "UTF8");
                    JSONArray jsonArray = new JSONArray(json);
                    JSONObject root = jsonArray.getJSONObject(0);

                    conditions.setLocalTime(root.getString("LocalObservationDateTime"));
                    String metric = (root.getJSONObject("Temperature").getString("Metric"));
                    JSONObject obj = new JSONObject(metric);
                    Metric metricObj = new Metric();
                    metricObj.setUnit(obj.getString("Unit"));
                    metricObj.setUnitType(obj.getString("UnitType"));
                    metricObj.setValue(obj.getString("Value"));
                    conditions.setMetric(metricObj);
                    conditions.setWeatherIcon(root.getString("WeatherIcon"));
                    if (10 > Integer.parseInt(conditions.getWeatherIcon())) {
                        conditions.setWeatherIcon("0".concat(conditions.getWeatherIcon()));
                    }
                    conditions.setWeatherText(root.getString("WeatherText"));

                    return conditions;
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
            }}

            return conditions;
        }

        @Override
        protected void onPostExecute(Conditions conditions) {
            super.onPostExecute(conditions);

            imageURL = "https://developer.accuweather.com/sites/default/files/*-s.png";

            setCurrentCityDetails(conditions);

            progressBar.setVisibility(View.INVISIBLE);

        }
    }

    public static void setCurrentCityDetails(Conditions conditions){

        if(conditions != null){
        if(savedCityList.size()>0){
            MainActivity.tv_cityDesc.setVisibility(View.INVISIBLE);
            MainActivity.tv_boxDesc.setVisibility(View.INVISIBLE);
            MainActivity.tv_savedCities.setVisibility(View.VISIBLE);

            MainActivity.recyclerView.setVisibility(View.VISIBLE);
            MainActivity.rv_adapter = new MyAdapter(MainActivity.savedCityList, MainActivity.contextMain);
            MainActivity.recyclerView.setAdapter(MainActivity.rv_adapter);
        }

        tv_currentCity.setVisibility(View.INVISIBLE);
        btn_setCity.setVisibility(View.INVISIBLE);

        tv_CityCountry.setVisibility(View.VISIBLE);
        tv_CityCountry.setText(currentCity + ", " + currentCountry);

        tv_weatherText.setVisibility(View.VISIBLE);
        tv_weatherText.setText(conditions.getWeatherText());

        tv_temp.setVisibility(View.VISIBLE);
        double temp = (Double.parseDouble(conditions.getMetric().getValue()) * (9/5)) + 32;
        tv_temp.setText( "Temperature: " + temp + " F");

        imageURL = imageURL.replace("*",conditions.getWeatherIcon());
        iv_weatherIcon.setVisibility(View.VISIBLE);
        Picasso.get().load(imageURL).into(iv_weatherIcon);

        tv_updated.setVisibility(View.VISIBLE);

        String pattern2 = "yyyy-MM-dd'T'HH:mm:ssZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern2);
        try {
            Date date = simpleDateFormat.parse(conditions.getLocalTime());
            PrettyTime p = new PrettyTime();
            tv_updated.setText(String.format("Updated: " + p.format(date)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }else{
            Toast.makeText(MainActivity.context, "City not found", Toast.LENGTH_SHORT).show();
        }
    }
}
