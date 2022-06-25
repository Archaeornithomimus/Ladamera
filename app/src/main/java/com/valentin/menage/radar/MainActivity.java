package com.valentin.menage.radar;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
import android.media.ToneGenerator;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private WifiManager wifiManager;
    private float proximityL;
    private float proximityC;
    private float proximityR;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiConfiguration wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", "ESP32-Access-Point");
        wifiConfig.preSharedKey = String.format("\"%s\"", "123456789");
        proximityL = 200;
        proximityC = 201;
        proximityR = 202;

        imageView = (ImageView) findViewById(R.id.carView);
        imageView.setImageResource(R.drawable.Voiture
        );
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        if (wifiManager.isWifiEnabled() != true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                startActivityForResult(panelIntent, 545);
            }
        }


        //remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();


        webView = (WebView) findViewById(R.id.cameraView);
        webView.setWebViewClient(new CameraView());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadUrl("http://192.168.4.1:81/stream");

        RequestQueue queue = Volley.newRequestQueue(this);
        String sensorUrl = "http://192.168.4.1:82/sensor";
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, sensorUrl,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Display the first 500 characters of the response string.
                                    Log.d("http request", "succes");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String[] value = response.split(",");
                                            proximityR = Float.parseFloat(value[0]);
                                            proximityC = Float.parseFloat(value[1]);
                                            proximityL = Float.parseFloat(value[2]);

                                            proximityDisplay();
                                            proximitySound();
                                            //mTextView.setText("Ready Player One");
                                        }
                                    });
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("http request", "error");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    queue.add(stringRequest);
                }
            }
        }).start();
    }


        /*if (Build.VERSION.SdkInt > BuildVersionCodes.Lollipop)
            mWebview.Settings.MixedContentMode = MixedContentHandling.CompatibilityMode;
        mWebview.Settings.SetPluginState(WebSettings.PluginState.On);
        mWebview.Settings.GetPluginState();
        mWebview.Settings.AllowFileAccess = true;*/
        /*MyView myViewCompteur = new MyView(this);
        LinearLayout myLayout1 = (LinearLayout)findViewById(R.id.myView);
        myLayout1.addView(myViewCompteur);
        setContentView(myViewCompteur);*/


    /*private boolean checkWifiOnAndConnected() {
        if (wifiManager.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                if (wifiInfo.getNetworkId() == -1) {
                    return false; // Not connected to an access point
                }
            } else{
                return false;
            }



            return true; // Connected to an access point
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    private void enableWifi(){
        if(wifiManager.isWifiEnabled()!=true){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                startActivityForResult(panelIntent, 545);
            }
        }
    }*/

    private boolean checkWifiState() {
        int state = wifiManager.getWifiState();

        if (state == wifiManager.WIFI_STATE_ENABLED) {
            Log.d("testWIFIEnable", "WifiActivé");
            return true;

        } else if (state == wifiManager.WIFI_STATE_ENABLING) {
            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    handler.postDelayed(this, 1000);
                }
            };
            handler.postDelayed(r, 1000);
            Log.d("testWIFIEnable", "après temps attente");
            return false;

        } else if (state == wifiManager.WIFI_STATE_UNKNOWN) {
            Log.d("testWIFIEnable", "mode inconnue");
            return false;

        } else if (state == wifiManager.WIFI_STATE_DISABLING) {
            Log.d("testWIFIEnable", "en désactivation");
            return false;

        } else if (state == wifiManager.WIFI_STATE_DISABLED) {
            Log.d("testWIFIEnable", "Wifi désactivé");
            return false;
        }
        Log.d("testWIFIEnable", "default case");
        return false;
    }

    public void proximityDisplay() {
        if ((proximityR <101) || (proximityC <101) || (proximityL <101)) {
            if ((proximityR <= proximityC) && (proximityR <= proximityL)) {
                imageView.setImageResource(R.drawable.voiture_right);
            } else if ((proximityC <= proximityR) && (proximityC <= proximityL)) {
                imageView.setImageResource(R.drawable.voiture_back);
            } else if ((proximityL <= proximityC) && (proximityL <= proximityR)) {
                imageView.setImageResource(R.drawable.voiture_left);
            }
        } else {
            imageView.setImageResource(R.drawable.Voiture);
        }
    }
    public void proximitySound(){
        if ((proximityR == 0) || (proximityL == 0) || (proximityC == 0)) {
            buzz(0);
            //Serial.println("zone0");
        } else if ((inRange(0, proximityR, 20)) | (inRange(0, proximityL, 20)) | (inRange(0, proximityC, 20))) {
            //Serial.println("zone1");
            buzz(1);
        } else if ((inRange(20, proximityR, 40)) | (inRange(20, proximityL, 40)) | (inRange(20, proximityC, 40))) {
            buzz(2);
            //Serial.println("zone2");
        } else if ((inRange(40, proximityR, 60)) | (inRange(40, proximityL, 60)) | (inRange(40, proximityC, 60))) {
            buzz(3);
            //Serial.println("zone3");
        } else if ((inRange(60, proximityR, 80)) | (inRange(60, proximityL, 80)) | (inRange(60, proximityC, 80))) {
            buzz(4);
            //Serial.println("zone4");
        } else if ((inRange(80, proximityR, 100)) | (inRange(80, proximityL, 100)) | (inRange(80, proximityC, 100))) {
            buzz(5);
            //Serial.println("zone5");
        }
    }

    public static void buzz(int zone) {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,
                100);
        switch (zone) {
            case 0:
                //toneG.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 200);
                //toneG.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 200);
                for (int i =0;i<6;i++){
                    toneG.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 100);
                    toneG.stopTone();
                }
            case 1:
                //toneG.startTone(ToneGenerator.TONE_CDMA_ANSWER, 200);
                for (int i =0;i<5;i++){
                    toneG.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 100);
                    toneG.stopTone();
                }
            case 2:
                //toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 200);
                for (int i =0;i<4;i++){
                    toneG.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 100);
                    toneG.stopTone();
                }
            case 3:
                for (int i =0;i<3;i++){
                    toneG.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 100);
                    toneG.stopTone();
                }
            case 4:
                for (int i =0;i<2;i++){
                    toneG.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 200);
                    toneG.stopTone();
                }

            case 5:
                    toneG.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 200);
                    toneG.stopTone();
            default:
                toneG.stopTone();
        }
    }

    public boolean inRange(float minimum, float val, float maximum) {
        if ((minimum <= val) && (val <= maximum)) {
            return true;
        } else {
            return false;
        }
    }
}