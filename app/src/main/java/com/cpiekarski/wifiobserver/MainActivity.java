
package com.cpiekarski.wifiobserver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.sql.Types.NULL;

/**
 * Android test app for monitoring Wifi state events using both the WifiManager
 * and the ConnectivityManager.
 */
public class MainActivity extends Activity {

    protected static final String TAG = "WifiObserverTest";
    protected static final String DATETAG = "DateTest";

    
    private TextView mWifiState;
    private TextView mWifiScan;
    private TextView mCMState;
    private TextView mWifiConfig;
    private AlarmManager alarmMgr;
    private WifiManager mWifiManager;
    private static final String WIFI_ACTION = "com.cpiekarski.wifiobserver.WIFI_ACTION";

    BroadcastReceiver mBr = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "Received Alarm Intent: "+mWifiManager.pingSupplicant());

            //get the last scan
            String scanList = "";
            List<ScanResult> sList = mWifiManager.getScanResults();
            Log.i(TAG, "----- Scan List -----");
            for(ScanResult sr : sList ) {
                Log.i(TAG, sr.toString());
                scanList += sr.toString();
            }
            mWifiScan.setText(scanList);
            Log.i(TAG, "------------------------");

            String configList = "";
            List<WifiConfiguration> wList= mWifiManager.getConfiguredNetworks();
            Log.i(TAG, "----- Configured List -----");
            for(WifiConfiguration wc : wList ) {
                Log.i(TAG, "SSID: "+wc.SSID);
                configList += wc.SSID;
            }
            mWifiConfig.setText(configList);
            Log.i(TAG, "------------------------");
            // request a new scan
            mWifiManager.startScan(); //doesn't wait
        }
    };

    private void dumpWifiInfo() {
        Log.v(TAG, "----- WifiManager Info -----");
        Log.v(TAG,"is5GHzBandSupported: "+mWifiManager.is5GHzBandSupported());
        Log.v(TAG,"isWifiEnabled: "+mWifiManager.isWifiEnabled());
        Log.v(TAG,"isTdlsSupported: "+mWifiManager.isTdlsSupported());
        Log.v(TAG,"isScanAlwaysAvailable: "+mWifiManager.isScanAlwaysAvailable());
        Log.v(TAG, "isP2pSupported:"+mWifiManager.isP2pSupported());
        Log.v(TAG,"isEnhancedPowerReportingSupported: "+mWifiManager.isEnhancedPowerReportingSupported());
        Log.v(TAG,"isDeviceToApRttSupported: "+mWifiManager.isDeviceToApRttSupported());
        Log.v(TAG,"-----------------------------");
    }
    
    private void testDate() {
        Date d = new Date();
        Calendar c = Calendar.getInstance();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        String sdfString = sdf.format(c.getTime());

        
        Log.v(DATETAG, "Date object data:");
        Log.v(DATETAG, "Month: "+d.getMonth());
        Log.v(DATETAG, "Day: "+d.getDate());
        Log.v(DATETAG, "Year: "+d.getYear()); //Returns the gregorian calendar year since 1900

        Log.v(DATETAG, "Calendar object data:");
        Log.v(DATETAG, "Month: " +c.get(Calendar.MONTH)); //0-11
        Log.v(DATETAG, "Day: "+c.get(Calendar.DAY_OF_MONTH));
        Log.v(DATETAG, "Year: "+c.get(Calendar.YEAR)); //Real year 2014
        
        Log.v(DATETAG, "SimpleDateFormat Tests:");
        Log.v(DATETAG, sdfString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.activity_main);
        registerConnectivityChange();
        
        mWifiState = (TextView) findViewById(R.id.wifi_state);
        mCMState = (TextView) findViewById(R.id.cm_state);
        mWifiScan = (TextView) findViewById(R.id.wifi_scan);
        mWifiConfig = (TextView) findViewById(R.id.wifi_config);
        
        testDate();

        mWifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        dumpWifiInfo();

        // Set up alarm interval
        IntentFilter intentFilter = new IntentFilter(WIFI_ACTION);
        this.registerReceiver(mBr, intentFilter);

        Intent aintent = new Intent(WIFI_ACTION);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 1024, aintent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                SystemClock.elapsedRealtime() +
                        5000, 5000,alarmIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Make and show Toast message.
     * @param text resource id of string message
     * {@code showToastBox(R.string.sdcard_not_readable);}
     */
    private void showToastBox(final int text) {
        runOnUiThread(new Runnable() {
             public void run() {
                 Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Make and show Toast message.
     * @param String text message
     * {@code showToastBox("I observe WiFi!");}
     */
    private void showToastBox(final String text) {
        runOnUiThread(new Runnable() {
             public void run() {
                 Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void registerConnectivityChange() {
        BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "In onReceive "+intent.getAction());
                
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    Log.d(TAG, "Connectivity Action Intent Received");
                    
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    //@SuppressWarnings("deprecation")
                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    NetworkInfo networkInfoCM = cm.getActiveNetworkInfo();
                    
                    Log.d(TAG, "NetworkInfo from Intent: " + networkInfo.toString());
                    //Log.d(TAG, "NetworkInfo from CM: " + networkInfoCM == null ? "null" : networkInfoCM.toString());

                    mCMState.setText(networkInfo.toString());
                    
                    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {

                        Log.d(TAG, "ConnectivityManager connected event");

                    } else {
                        Log.d(TAG, "ConnectivityManager disconnected event");
                    }
                
                } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo networkInfo = intent
                            .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
    
                    Log.d(TAG, "Wifi Network State Changed: "+intent.toString());
                    Log.d(TAG, networkInfo.toString());
                    Log.d(TAG, "Reason: "+networkInfo.getReason());
                    Log.d(TAG, "State: "+intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0));
                    Log.d(TAG, "State: "+intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, 0));
                    
                    mWifiState.setText(networkInfo.toString());
                    
                    if(networkInfo.isConnected()) {
                        Log.d(TAG, "WifiManager says we're connected");
                    } else {
                        Log.d(TAG, "WifiManager says we're not connected");
                    }
                } 
            }
        };

        registerReceiver(connectionReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        registerReceiver(connectionReceiver, new IntentFilter("android.net.wifi.STATE_CHANGE"));

    }
}
