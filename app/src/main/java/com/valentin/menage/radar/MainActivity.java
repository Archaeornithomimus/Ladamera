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
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiConfiguration wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", "ESP32-Access-Point");
        wifiConfig.preSharedKey = String.format("\"%s\"", "123456789");


        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        if (wifiManager.isWifiEnabled() != true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
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
            Log.d("testWIFIEnable","WifiActivé");
            return true;

        } else if (state == wifiManager.WIFI_STATE_ENABLING) {
            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    handler.postDelayed(this, 1000);
                }
            };
            handler.postDelayed(r, 1000);
            Log.d("testWIFIEnable","après temps attente");
            return false;

        } else if (state == wifiManager.WIFI_STATE_UNKNOWN) {
            Log.d("testWIFIEnable","mode inconnue");
            return false;

        } else if (state == wifiManager.WIFI_STATE_DISABLING) {
            Log.d("testWIFIEnable","en désactivation");
            return false;

        } else if (state == wifiManager.WIFI_STATE_DISABLED) {
            Log.d("testWIFIEnable","Wifi désactivé");
            return false;
        }
        Log.d("testWIFIEnable","default case");
        return false;
    }
}