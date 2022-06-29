package com.valentin.menage.radar;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
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
    private SoundPool soundPool;
    private AudioManager audioManager;
    private float volume;
    private boolean loaded;
    public int soundIdOn =0;
    public int soundIdOff =0;

    final long numCycles0 = 24;
    final long numCycles1 = 20;
    final long numCycles2 = 16;
    final long numCycles3 = 8;
    final long numCycles4 = 4;
    final long numCycles5 = 1;
    final long delayValue = 1000000 / 2000;
    Handler handler;

    private static final int streamType = AudioManager.STREAM_ALARM;
    private int streamId;

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

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        handler = new Handler();
        // Current volumn Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volumn (0 --> 1)
        volume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);

        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21 ) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(1);

            this.soundPool = builder.build();
        }
        // for Android SDK < 21
        else {
            //SoundPool(int maxStreams, int streamType, int srcQuality);
            //this.soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        soundIdOn = this.soundPool.load(this,R.raw.beep_sample,1);
        soundIdOff = this.soundPool.load(this,R.raw.beep_sample_off,1);


        imageView = (ImageView) findViewById(R.id.carView);
        imageView.setImageResource(R.drawable.voiture);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        if (wifiManager.isWifiEnabled() != true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                startActivityForResult(panelIntent, 545);
            }
        }

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
            imageView.setImageResource(R.drawable.voiture);
        }
    }
    public void proximitySound(){
        if ((proximityR == 0) || (proximityL == 0) || (proximityC == 0)) {
            buzz(0);
        } else if ((inRange(0, proximityR, 20)) | (inRange(0, proximityL, 20)) | (inRange(0, proximityC, 20))) {
            buzz(1);
        } else if ((inRange(20, proximityR, 40)) | (inRange(20, proximityL, 40)) | (inRange(20, proximityC, 40))) {
            buzz(2);
        } else if ((inRange(40, proximityR, 60)) | (inRange(40, proximityL, 60)) | (inRange(40, proximityC, 60))) {
            buzz(3);
        } else if ((inRange(60, proximityR, 80)) | (inRange(60, proximityL, 80)) | (inRange(60, proximityC, 80))) {
            buzz(4);
        } else if ((inRange(80, proximityR, 100)) | (inRange(80, proximityL, 100)) | (inRange(80, proximityC, 100))) {
            buzz(5);
        }
    }

    public void buzz(int zone) {
        switch (zone) {
            case 0:
                playSoundSleep(numCycles0);
            case 1:
                playSoundSleep(numCycles1);
            case 2:
                playSoundSleep(numCycles2);
            case 3:
                playSoundSleep(numCycles3);
            case 4:
                playSoundSleep(numCycles4);
            case 5:
                playSoundSleep(numCycles5);
            default:
        }
    }

    private void playSoundSleep(long numCycles) {
        if (loaded == true) {
            float leftVolumn = volume;
            float rightVolumn = volume;
            streamId = soundPool.play(soundIdOn, leftVolumn, rightVolumn, 0, 0, 1);
            for (long i = 0; i < numCycles - 1; i++) {
                handler.postDelayed(new Runnable() {
                    public void run() {
                        streamId = soundPool.play(soundIdOff, leftVolumn, rightVolumn, 0, 0, 1);
                    }
                }, delayValue);

                handler.postDelayed(new Runnable() {
                    public void run() {
                        streamId = soundPool.play(soundIdOn, leftVolumn, rightVolumn, 0, 0, 1);
                    }
                }, delayValue);
            }
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