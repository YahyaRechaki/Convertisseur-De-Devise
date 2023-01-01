package com.example.convertisseurdedevise;

import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import okhttp3.*;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {


    private static final int MESSAGE_SUCCESS = 1;
    ArrayList<String> arrayList = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_SUCCESS) {
                String response = (String) msg.obj;
                // Handle response string here
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject symbols = responseJson.getJSONObject("symbols");
                    Log.d("msgConsole3", String.valueOf(symbols));
                    Spinner spinner1 = findViewById(R.id.spinner1);
                    Spinner spinner2 = findViewById(R.id.spinner2);
                    Iterator<String> keys = symbols.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = symbols.getString(key);
                        Log.d("getClass1", key.getClass().getName());
                        Log.d("getClass2", value.getClass().getName());
                        // Do something with the key and value
                        Log.d("valVal", value);
                        Log.d("keyKey", key);
                        arrayList.add(key);
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner1.setAdapter(arrayAdapter);
                    spinner2.setAdapter(arrayAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EditText amountEditText = findViewById(R.id.montant);
        Spinner deSpinner = findViewById(R.id.spinner1);
        Spinner aSpinner = findViewById(R.id.spinner2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner1 = findViewById(R.id.spinner1);
        Spinner spinner2 = findViewById(R.id.spinner2);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = parent.getItemAtPosition(position).toString();
                Log.d("selectedCurrency", selectedCurrency);
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });
        getAllCurrencies();
        convertAmount();
        Log.d("msgConsole1", "Hiiiiiiii");
    }
    protected void convertAmount(){
        EditText amountEditText = findViewById(R.id.montant);
        Spinner deSpinner = findViewById(R.id.spinner1);
        Spinner aSpinner = findViewById(R.id.spinner2);
                // Perform network operation here
                Button requestButton = findViewById(R.id.convertir);
                requestButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // the button was clicked
                            OkHttpClient client = new OkHttpClient().newBuilder().build();

                            Request request = new Request.Builder()
                                    .url("https://api.apilayer.com/exchangerates_data/convert?to="+aSpinner.getSelectedItem().toString()+"&from="+deSpinner.getSelectedItem().toString()+"&amount="+amountEditText.getText().toString())
                                    .addHeader("apikey", "SwjMbc3cYC2YGNDPjNrmmRvprGjG5HiM")
                                    .method("GET", null)
                                    .build();
                            Response response = null;
                            Log.d("aSpinner : ", aSpinner.getSelectedItem().toString());
                            Log.d("deSpinner : ", deSpinner.getSelectedItem().toString());
                            Log.d("amountEditText : ", amountEditText.getText().toString());
                            try {
                                response = client.newCall(request).execute();
                                final LinearLayout layout = findViewById(R.id.layoutResultat);
                                String responseString = response.body().string();
                                Log.d("responseString", responseString);
                                JSONObject objResponseString = new JSONObject(responseString);
                                String result = objResponseString.getString("result");
                                String finalResult = result;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );

                                        //TextView textView = new TextView(getApplicationContext());
                                        TextView textView = new TextView(MainActivity.this);
                                        textView.setLayoutParams(params);
                                        textView.setText(String.valueOf(finalResult));
                                        Log.d("finalResult", String.valueOf(finalResult));
                                        layout.removeAllViews();
                                        layout.addView(textView);
                                        Log.d("layout", String.valueOf(layout));
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }}).start();
                    }
                });
    }

    protected void getAllCurrencies() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Perform network operation here
                OkHttpClient client = new OkHttpClient().newBuilder().build();
                Request request = new Request.Builder()
                        .url("https://api.apilayer.com/exchangerates_data/symbols")
                        .addHeader("apikey", "SwjMbc3cYC2YGNDPjNrmmRvprGjG5HiM")
                        .method("GET", null)
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    String responseString = response.body().string();
                    Message message = new Message();
                    message.what = MESSAGE_SUCCESS;
                    message.obj = responseString;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}