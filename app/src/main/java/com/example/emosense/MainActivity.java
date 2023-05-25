package com.example.emosense;



import static com.google.android.gms.fitness.data.HealthDataTypes.AGGREGATE_OXYGEN_SATURATION_SUMMARY;
import static com.google.android.gms.fitness.data.HealthDataTypes.TYPE_OXYGEN_SATURATION;

import static org.json.JSONObject.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.material.chip.Chip;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient googleSignInClient;
    private GoogleSignInOptions googleSignInOptions;
    private static final int RC_SIGN_IN = 9001;
    List<DataItem> songsList;
    int heartRate=0;
    int bloodOxygen=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        ImageView view = findViewById(R.id.imageView);
         heartRate=0;
         bloodOxygen=0;


        StrictMode.setThreadPolicy(policy);
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            // Launch the sign-in flow
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);


        }


        FitnessOptions fitnessOptions = FitnessOptions.builder()

                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .addDataType(AGGREGATE_OXYGEN_SATURATION_SUMMARY, FitnessOptions.ACCESS_READ)


                .build();

// Get a GoogleSignInClient object
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        // ProgressBar progressBar = view.findViewById(R.id.progressBar);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (heartRate<=60 && bloodOxygen>=95){
                    String jsonString= null;

                    try {
                        jsonString = make_call("happy");
                        generateList(jsonString);
                        gotoPLayer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                else if( heartRate>60&& heartRate<100&& bloodOxygen>=95){
                    String jsonString= null;

                    try {
                        jsonString = make_call("neutral");
                        generateList(jsonString);
                        gotoPLayer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                else if( heartRate>100&& bloodOxygen>=95){
                    String jsonString= null;

                    try {
                        jsonString = make_call("sad");
                        generateList(jsonString);
                        gotoPLayer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

// Check if the user is signed in
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            // Launch the sign-in flow
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);


        } else {
            if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
                // Launch the permissions dialog
                GoogleSignIn.requestPermissions(
                        this,
                        100,
                        GoogleSignIn.getLastSignedInAccount(this),
                        fitnessOptions);

            } else {

                long endTime = System.currentTimeMillis();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startTime = calendar.getTimeInMillis();


                DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                        .aggregate(TYPE_OXYGEN_SATURATION, AGGREGATE_OXYGEN_SATURATION_SUMMARY)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(10, TimeUnit.MINUTES)
                        .setLimit(1)
                        .build();

                Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                        .readData(readRequest)
                        .addOnSuccessListener(dataReadResponse -> {
                            // Get the read data
                            List<Bucket> buckets = dataReadResponse.getBuckets();
                            for (Bucket bucket : buckets) {
                                String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                        .format(bucket.getStartTime(TimeUnit.MILLISECONDS));
                                String text = "Date: " + date + "\n";
                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet dataSet : dataSets) {
                                    String type = dataSet.getDataType().getName();
                                    for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                        String value = String.valueOf(dataPoint.getValue(dataPoint.getDataType().getFields().get(0)));
                                        text += type + ": " + value + "\n";
                                        Log.d("TAG", text);
                                        if(type.contains("heart_rate")){
                                            heartRate= (int)Double.parseDouble(value);
                                        }
                                        if(type.contains("oxygen_saturation")){
                                            bloodOxygen= (int)Double.parseDouble(value);
                                        }

                                    }
                                }

                                Log.d("TAG", text);
                                // Display text in a text view
                                //  vitals.append(text + "\n");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("TAG", "There was a problem getting the data.", e);
                        })
                        .addOnCompleteListener(task -> {
                            // progressBar.setVisibility(View.GONE);
                            // Toast.makeText(getContext(), "Data Imported", Toast.LENGTH_SHORT).show();

                        });

            }
        }



        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("980102181758-g4fjk2qk74r03octrnc7avnmivrcs2rm.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);



        Chip myChipButton = findViewById(R.id.happy_chip_button);
        myChipButton.setChecked(false);

        myChipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String jsonString= make_call("happy");
                    generateList(jsonString);
                    gotoPLayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Chip myChipButton1 = findViewById(R.id.neutral_chip_button);
        myChipButton1.setChecked(false);
        myChipButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String jsonString= make_call("neutral");
                    generateList(jsonString);
                    gotoPLayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Intent intent = new Intent(this, PlayerList.class);
        Bundle args = new Bundle();


        Chip myChipButton2 = findViewById(R.id.sad_chip_button);
        myChipButton2.setChecked(false);

        myChipButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String jsonString= make_call("sad");
                    generateList(jsonString);
                    gotoPLayer();


                    }
                 catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

  private String make_call(String mood) throws IOException {
      URL url = new URL("https://white-glacier-1ca1b71c9ee24f37b5ba22fe1ccb3e7d.azurewebsites.net/recommend_songs");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json");
      con.setDoOutput(true);
      String data = "{\"user_mood\":\""+mood+"\"}";
      OutputStream os = con.getOutputStream();
      byte[] input = data.getBytes("utf-8");
      os.write(input, 0, input.length);

      int responseCode = con.getResponseCode();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
      }
      in.close();

      Log.e("Response code: ",  ""+responseCode);
      Log.e("Response body: " ,""+ response.toString());

      os.flush();
      os.close();
      con.disconnect();



      return response.toString();
  }


  public void generateList(String jsonString){
      try {
          JSONObject jsonObject = new JSONObject(jsonString);
          JSONObject songsObject = jsonObject.getJSONObject("recommended_songs");
          JSONObject artistObject = songsObject.getJSONObject("artist");
          JSONObject idObject = songsObject.getJSONObject("id");
          JSONObject titleObject = songsObject.getJSONObject("title");
          songsList = new ArrayList<>();
          Iterator<String> keys = idObject.keys(); // use the keys() method instead
          while (keys.hasNext()) {
              String id = keys.next();
              String trackName = titleObject.getString(id);
              String artistName = artistObject.getString(id);
              String url = "https://open.spotify.com/track/" + idObject.getString(id);
              DataItem dataItem = new DataItem(artistName, trackName, url);
              songsList.add(dataItem);
          }
      } catch (JSONException e) {
          e.printStackTrace();
      }
  }

  public void gotoPLayer(){
      Intent intent = new Intent(this, PlayerList.class);
      Bundle args = new Bundle();
      args.putSerializable("ARRAYLIST",(Serializable)songsList);
      intent.putExtra("BUNDLE",args);
      startActivity(intent);
  }
}