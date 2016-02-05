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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.ArrayAdapter;
import android.content.BroadcastReceiver;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;



public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {
 // ListView lv;
  WifiManager wifi;
  String wifis[];
  WifiScanReceiver wifiReciever;
  ParseObject testObject;
  TextView roomSelected;
  String room;
  //spinner
  Spinner roomsList;
  Spinner directionsList;

  ArrayAdapter<String> roomsAdapter;
  ArrayAdapter<String> directionAdapter;

  boolean spinnerOnItemInit=false;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ParseAnalytics.trackAppOpenedInBackground(getIntent());
    roomsList = (Spinner)findViewById(R.id.roomsList);
    directionsList = (Spinner)findViewById(R.id.directionList);

    //rooms adapter setting
    ArrayAdapter<CharSequence> adapterRooms = ArrayAdapter.createFromResource(this,R.array.rooms, R.layout.support_simple_spinner_dropdown_item);
    adapterRooms.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
    roomsList.setAdapter(adapterRooms);
    roomsList.setOnItemSelectedListener(this);

    //directions adapter settings
    ArrayAdapter<CharSequence> adapterDirection = ArrayAdapter.createFromResource(this,R.array.directions,R.layout.support_simple_spinner_dropdown_item);
    adapterDirection.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
    directionsList.setAdapter(adapterDirection);



    //wifi indoor code starts here
    //lv=(ListView)findViewById(R.id.listView);
    wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
    wifiReciever = new WifiScanReceiver();


    //real time code
/*    wifi.startScan();

    ScheduledThreadPoolExecutor exec  = new ScheduledThreadPoolExecutor(1);
    long delay = 2000;
    exec.scheduleWithFixedDelay(new MyTask(),0,delay, TimeUnit.MILLISECONDS);*/
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

    public void SendToParse( String ss, String ssid ){

    testObject = new ParseObject("TestObject");
    //sending data to parse
    //testObject.put("xPos", x);
    //testObject.put("yPos", y);
   // testObject.put("direction", direc);
    testObject.put("signalStrength", ss);
    testObject.put("SSID", ssid);
    testObject.saveInBackground();
Log.i("sent","sent");

  }

//real time code
/*  public void gotoImageActivity(View view)
  {
    Intent intent = new Intent(this,MapActivity.class);
    startActivity(intent);
  }

  public void sendDataToParseForEachRoom(View view)
  {
    Intent intent = new Intent(this,SendDataTesting.class);
    startActivity(intent);
  }*/

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    //because this listener is called on initialization, used bool to stop that init.
    if(spinnerOnItemInit) {
      int i=0;
      roomSelected = (TextView) view;

      room = roomSelected.getText().toString();
      Log.i("Selected : ", room.toString());
      //Toast.makeText(this, "You selected : " + room, Toast.LENGTH_SHORT).show();
      while(i<1)
      {
        //new MyTask().run();
       /* Thread thread = new Thread(){
          @Override
        public void run(){
            wifi.startScan();
          }
        };
        thread.start();*/
        wifi.startScan();
        i++;

      }
    }
    spinnerOnItemInit=true;

  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }

/*
  public class MyTask implements Runnable{
    @Override
    public void run(){
      wifi.startScan();

    }
  }*/


  //whenever wifi is receiver, it is intercepted here
  private class WifiScanReceiver extends BroadcastReceiver{

    public void onReceive(Context c, Intent intent) {

      List<ScanResult> wifiScanList = wifi.getScanResults();
      Log.i("Connections : ", String.valueOf(wifiScanList.size()));
      //wifis = new String[3];
      MainActivity obj = new MainActivity();
      int j=0;
      for(int i = 0; i < wifiScanList.size(); i++){
        String ssid = (wifiScanList.get(i).SSID).toString();

          if((ssid.equals("dlink-7D8C"))||(ssid.equals("dlink-95A8"))||(ssid.equals("dlink-7D28"))) {

            /*Log.i("dlink","dlink");
            wifis[j] = ssid + "  " + String.valueOf((wifiScanList.get(i).level));
            j++;*/
            Log.i("dlink","dlink");
          obj.SendToParse( String.valueOf(wifiScanList.get(i).level), String.valueOf(wifiScanList.get(i).SSID));
        }
      }
      //lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, wifis));
    }


  }

}
