/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.parse.ParseObject;
import com.parse.ParseAnalytics;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.ArrayAdapter;
import android.content.BroadcastReceiver;


import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;



public class MainActivity extends Activity {
  ListView lv;
  WifiManager wifi;
  String wifis[];
  WifiScanReceiver wifiReciever;
  ParseObject testObject;


  //Intent intent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //parse initialization
    ParseAnalytics.trackAppOpenedInBackground(getIntent());

    //wifi indoor code starts here
    lv=(ListView)findViewById(R.id.listView);
    wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
    wifiReciever = new WifiScanReceiver();
    //intent = new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);


   /* Runnable runnable = new Runnable() {
      @Override
      public void run() {
        long endTime = System.currentTimeMillis()+2000;
        while(System.currentTimeMillis() < endTime){
          synchronized (this){
            try{
              wait(endTime-System.currentTimeMillis());
            }catch (Exception e){}
          }
        }

      }
    };*/
    // registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    wifi.startScan();


    ScheduledThreadPoolExecutor exec  = new ScheduledThreadPoolExecutor(1);
    long delay = 2000;
    exec.scheduleWithFixedDelay(new MyTask(),0,delay, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  protected void onPause() {
    unregisterReceiver(wifiReciever);
    super.onPause();
  }

  protected void onResume() {
    registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    super.onResume();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void SendToParse(String x, String y, String direc, String ss, String ssid ){

    testObject = new ParseObject("TestObject");
    //sending data to parse
    testObject.put("xPos", x);
    testObject.put("yPos", y);
    testObject.put("direction", direc);
    testObject.put("signalStrength", ss);
    testObject.put("SSID", ssid);
    testObject.saveInBackground();
  }


  public void gotoImageActivity(View view)
  {
    Intent intent = new Intent(this,MapActivity.class);
    startActivity(intent);
  }

  public void sendDataToParseForEachRoom(View view)
  {
    Intent intent = new Intent(this,SendDataTesting.class);
    startActivity(intent);
  }



  public class MyTask implements Runnable{
    @Override
    public void run(){
      wifi.startScan();
    }
  }




  //whenever wifi is receiver, it is intercepted here
  private class WifiScanReceiver extends BroadcastReceiver{

    public void onReceive(Context c, Intent intent) {

      List<ScanResult> wifiScanList = wifi.getScanResults();
      Log.i("Connections : ", String.valueOf(wifiScanList.size()));
      wifis = new String[wifiScanList.size()];
      MainActivity obj = new MainActivity();

      for(int i = 0; i < wifiScanList.size(); i++){
        String ssid = (wifiScanList.get(i).SSID).toString();
        wifis[i] = ssid + "  " + String.valueOf((wifiScanList.get(i).level));
        if((ssid.equals("CCSecure"))) {
          //Log.i("asd", ((wifiScanList.get(i).SSID).toString()));
          //obj.SendToParse("1", "1", "N", String.valueOf(wifiScanList.get(i).level), String.valueOf(wifiScanList.get(i).SSID));
        }
        //Log.i("Level : ", wifiScanList.get(i).SSID + " : " + String.valueOf(wifiScanList.get(i).level));
      }
      lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, wifis));
    }


  }

}
