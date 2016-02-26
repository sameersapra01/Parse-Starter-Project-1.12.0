package com.parse.starter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.os.Handler;

/**
 * Created by samee on 2016-02-01.
 */
public class SendDataTesting extends Activity {

    //Our 3 routers
    private static final String Router95A8  = "dlink-95A8";
    private static final String Router7D28  = "dlink-7D28";
    private static final String Router7D8C  = "dlink-7D8C";

    //number of real time data
    private static final int numberOfTimesRTData = 5;

    //Number of Off-line mean data
    private static final int numberOfOffLineMeanData = 112;

    //variables required for wifi scanning
    WifiManager wifi;
    String wifis[];
    WifiScanReceiver wifiReciever;

    //parse variable
    ParseObject testObject;

    //UI variable
    TextView ch;
    TextView ch1;
    TextView ch0;

    //list of class containg 6 properties.
    List<DataPoint> dataPoints;

    //level of each router.
    int Router95A8Level =0;
    int Router7D28Level=0;
    int Router7D8CLevel=0;

    //mean and sum of real time values.
    double meanOfRouter95A8 = 0;
    double meanOfRouter7D28 = 0;
    double meanOfRouter7D8C = 0;

    //bool values to manage the threads
    boolean calculationsForDataPointControl = false;
    boolean RTDataThreadControl = true;

    //int i=0;

    //counter for gettin off-line mean data
    int countOffLineMean=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.data_testing);
            ch = (TextView) findViewById(R.id.testingText);
            ch1 = (TextView) findViewById(R.id.testingText1);
            ch0 = (TextView) findViewById(R.id.realTimeLoc);


            //initializing list of DataPoint class.
            dataPoints = new ArrayList<DataPoint>();

            //setting wifi manger
            wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiReciever = new WifiScanReceiver();

            HandlerThread handlerThread = new HandlerThread("ht");
            handlerThread.start();
            Looper looper = handlerThread.getLooper();
            Handler handler = new Handler(looper);

            registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),null,handler);

            //get the mean from parse and store it in a list of DataPoint class in a thread
            Thread th1 = new Thread(new getMeanFromParse());
            //start the thread
            th1.start();

            //wait for this thread to get all data and die
            th1.join();

            Toast.makeText(this,"Got all data",Toast.LENGTH_SHORT).show();


/*            //Update Location thread
            Thread updateLocationThread = new Thread( new UpdateLocation());
            updateLocationThread.start();*/



/*            //problems are the old created threads will show the old location for a new position...
            Thread realTimeDP = new Thread(new CreateNewUpdateLocationThreads());
            realTimeDP.start();*/

            //can be in one single thread instead of calling it every 2 seconds.
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            long delay = 100;
            exec.scheduleWithFixedDelay(new UpdateLocation(), 0, delay, TimeUnit.MILLISECONDS);

        }
        catch (Exception ex){
            Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
       // registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void onPause() {
        try{
           // unregisterReceiver(wifiReciever);
            super.onPause();}
        catch (Exception ex){
        }
    }

    public void btnClick(View view)
    {
        try {
            if (dataPoints.size() > 0) {
                Toast.makeText(getBaseContext(), String.valueOf(dataPoints.size()), Toast.LENGTH_SHORT).show();
                /*for (DataPoint dp:dataPoints
                     ) {
                    Log.i("Dp : " , dp.direction);
                }*/
            }
        }
        catch (Exception ex)
        {
            Toast.makeText(this,"Erro occured...",Toast.LENGTH_SHORT).show();
        }
    }

    public class CreateNewUpdateLocationThreads implements Runnable{
        @Override
        public void run() {
            try {
                int numberOfThreads = 0;
                while (numberOfThreads < 3) {
                    Thread locationThread = new Thread(new UpdateLocation());
                    locationThread.start();
                    numberOfThreads++;
                }

            }
            catch (Exception ex)
            {
                Toast.makeText(getBaseContext(),"Error occured in creating thread...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class UpdateLocation implements  Runnable{
        @Override
        public void run() {
            try {
                int i = 0;
                if (RTDataThreadControl)
                {
                    //setting the value of all 3 router's level to 0
                    Router7D28Level = 0;
                    Router7D8CLevel = 0;
                    Router95A8Level = 0;

                    //compare the real time data with mean data and update the position
                    while (i < numberOfTimesRTData) {
                        wifi.startScan();
                        if (calculationsForDataPointControl) {
                            //get the mean of 3 routers fo 5 rows
                            //Log.i("sum 3", String.valueOf(i) + " " + String.valueOf(Router95A8Level));
                            i++;
                            calculationsForDataPointControl = false;
                            RTDataThreadControl = false;
                        }
                    }
                    //calculating the mean of 3 different RT routers.
                    meanOfRouter7D28 = Router7D28Level / numberOfTimesRTData;
                    meanOfRouter7D8C = Router7D8CLevel / numberOfTimesRTData;
                    meanOfRouter95A8 = Router95A8Level / numberOfTimesRTData;

                    Log.i("mean of 7D28", String.valueOf(meanOfRouter7D28));
                    Log.i("mean of 7D8C", String.valueOf(meanOfRouter7D8C));
                    Log.i("mean of 95A8", String.valueOf(meanOfRouter95A8));

               /*     SendDataTesting.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ch1.setText(String.valueOf(Router7D28Level) + " " + String.valueOf(Router7D8CLevel) + " " + String.valueOf(Router95A8Level));
                            ch.setText(String.valueOf(meanOfRouter7D28) + " " + String.valueOf(meanOfRouter7D8C) + " " + String.valueOf(meanOfRouter95A8));
                        }
                    });*/


                /*        //compare RT mean with the off-line mean data and update the position on the map
                    for (DataPoint dp:dataPoints) {

                        //do linear search

                        //exact search
                        if(dp.dlink95A8.equals(String.valueOf(meanOfRouter95A8))&&dp.dlink7D28.equals(String.valueOf(meanOfRouter7D28))
                                &&dp.dlink7D8C.equals(String.valueOf(meanOfRouter7D8C)))
                        {
                            //found the location and update it on the map
                            Toast.makeText(getBaseContext(),dp.xPos + "  " + dp.yPos,Toast.LENGTH_SHORT).show();
                        }*/
                        //get the nearest router
                            //meanOfRouter95A8 is neareset to the user
                        if (meanOfRouter95A8 > meanOfRouter7D28  && meanOfRouter95A8 > meanOfRouter7D8C)
                        {
                            //meanOfRouter95A8 is neareset to the user
                           // Toast.makeText(getBaseContext() , Router95A8.toString(),Toast.LENGTH_SHORT).show();
                            Log.i("correct11111", String.valueOf(meanOfRouter95A8));
                            SendDataTesting.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ch0.setText("near : 95A8");
                                }
                            });
                          //  break;
                        }

                            //meanOfRouter7D8C is nearest to the user
                        else if(meanOfRouter7D8C > meanOfRouter7D28 && meanOfRouter7D8C > meanOfRouter95A8)
                        {
                            //meanOfRouter7D8C is nearest to the user
                            //Toast.makeText(getBaseContext() , Router7D8C.toString(),Toast.LENGTH_SHORT).show();
                            Log.i("correct22222", String.valueOf(meanOfRouter7D8C));
                            SendDataTesting.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ch0.setText("near : 7D8C");
                                }
                            });
                          //  break;
                        }

                        //meanOfRouter7D28 is nearest to the user
                        else if(meanOfRouter7D28 > meanOfRouter95A8 && meanOfRouter7D28 > meanOfRouter7D8C)
                        {
                            //meanOfRouter7D28 is nearest to the user
                           // Toast.makeText(getBaseContext() , Router7D28.toString(),Toast.LENGTH_SHORT).show();
                            Log.i("correct333333", String.valueOf(meanOfRouter7D28));
                            SendDataTesting.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ch0.setText("near : 7D28");
                                }
                            });
                            //break;
                        }
                    }

                    //start another thread
                    RTDataThreadControl = true;
                //}

            }
            catch (Exception ex)
            {
                Toast.makeText(getBaseContext(),ex.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            try {
                List<ScanResult> wifiScanList = wifi.getScanResults();

                for (int i = 0; i < wifiScanList.size(); i++) {
                    String ssid = (wifiScanList.get(i).SSID).toString();
                    if ((ssid.equals(Router7D8C))){

                        Router7D8CLevel += wifiScanList.get(i).level;
                    }
                    else if(ssid.equals(Router95A8)) {
                        Router95A8Level += wifiScanList.get(i).level;
                    }
                    else if((ssid.equals(Router7D28))){
                        Router7D28Level += wifiScanList.get(i).level;
                    }
                }
                calculationsForDataPointControl = true;
                //i++;
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*public class UpdateLocation implements  Runnable{

        //level of each router.
        int Router95A8Level =0;
        int Router7D28Level=0;
        int Router7D8CLevel=0;

        //mean and sum of real time values.
        double meanOfRouter95A8 = 0;
        double meanOfRouter7D28 = 0;
        double meanOfRouter7D8C = 0;

        @Override
        public void run() {
            try {
                int i = 0;

                //compare the real time data with mean data and update the position
                while (i < numberOfTimesRTData) {
                    wifi.startScan();
                    if (calculationsForDataPointControl) {
                        //get the mean of 3 routers fo 5 rows
                        //Log.i("sum 3", String.valueOf(i) + " " + String.valueOf(Router95A8Level));
                        i++;
                        calculationsForDataPointControl = false;
                        RTDataThreadControl = false;
                    }
                }
                //calculating the mean of 3 different RT routers.
                meanOfRouter7D28 = Router7D28Level / numberOfTimesRTData;
                meanOfRouter7D8C = Router7D8CLevel / numberOfTimesRTData;
                meanOfRouter95A8 = Router95A8Level / numberOfTimesRTData;

                Log.i("mean of 7D28", String.valueOf(meanOfRouter7D28));
                Log.i("mean of 7D8C", String.valueOf(meanOfRouter7D8C));
                Log.i("mean of 95A8", String.valueOf(meanOfRouter95A8));

             *//*   SendDataTesting.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ch1.setText(String.valueOf(Router7D28Level) + " " + String.valueOf(Router7D8CLevel) + " " + String.valueOf(Router95A8Level));
                        ch.setText(String.valueOf(meanOfRouter7D28) + " " + String.valueOf(meanOfRouter7D8C) + " " + String.valueOf(meanOfRouter95A8));
                    }
                });*//*


                    //compare RT mean with the off-line mean data and update the position on the map
               *//* for (DataPoint dp:dataPoints) {

                    //do linear search

                    //exact search
                    if(dp.dlink95A8.equals(String.valueOf(meanOfRouter95A8))&&dp.dlink7D28.equals(String.valueOf(meanOfRouter7D28))
                            &&dp.dlink7D8C.equals(String.valueOf(meanOfRouter7D8C)))
                    {
                        //found the location and update it on the map
                        Toast.makeText(getBaseContext(),dp.xPos + "  " + dp.yPos,Toast.LENGTH_SHORT).show();
                    }

                    //find the possible positions from off-line mean
                    if(true)
                    {

                    }
                }
*//*

            }
            catch (Exception ex)
            {
                Toast.makeText(getBaseContext(),ex.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }

        public class WifiScanReceiver extends BroadcastReceiver {
            public void onReceive(Context c, Intent intent) {
                try {
                    List<ScanResult> wifiScanList = wifi.getScanResults();

                    for (int i = 0; i < wifiScanList.size(); i++) {
                        String ssid = (wifiScanList.get(i).SSID).toString();
                        if ((ssid.equals(Router7D8C))){

                            Router7D8CLevel += wifiScanList.get(i).level;
                        }
                        else if(ssid.equals(Router95A8)) {
                            Router95A8Level += wifiScanList.get(i).level;
                        }
                        else if((ssid.equals(Router7D28))){
                            Router7D28Level += wifiScanList.get(i).level;
                        }
                    }
                    calculationsForDataPointControl = true;
                    //i++;
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }*/







    public class getMeanFromParse implements Runnable{

        int y = 1;
        @Override
        public void run() {

            try {
                while (y < 29) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("MeanData2");
                    query.whereEqualTo("xPos", "1");
                    query.whereEqualTo("yPos", String.valueOf(y));
                    query.setLimit(4);
                    query.findInBackground(new FindCallback<ParseObject>() {

                        public void done(List<ParseObject> scoreList, ParseException e) {
                            if (e == null) {
                                Log.i("score", "Retrieved " + scoreList.size() + " scores");

                                //for rows for each direction representing a single location
                                for (int i = 0; i < 4; i++) {
                                    //collect data
                                    String xPos = scoreList.get(i).getString("xPos");
                                    String yPos = scoreList.get(i).getString("yPos");
                                    String direction = scoreList.get(i).getString("direction");
                                    String dlink7d28 = scoreList.get(i).getString("dlink7D28");
                                    String dlink95A8 = scoreList.get(i).getString("dlink95A8");
                                    String dlink7d8c = scoreList.get(i).getString("dlink7D8C");

                                    DataPoint dataPoint = new DataPoint();
                                    dataPoint.xPos = xPos;
                                    dataPoint.yPos = yPos;
                                    dataPoint.direction = direction;
                                    dataPoint.dlink7D28 = dlink7d28;
                                    dataPoint.dlink7D8C = dlink7d8c;
                                    dataPoint.dlink95A8 = dlink95A8;
                                    dataPoints.add(dataPoint);

                                    countOffLineMean++;
                                }
                            } else {
                                Log.d("score", "Error: " + e.getMessage());
                            }
                        }
                    });
                    y++;
                }

            }catch (Exception ex)
            {
                Toast.makeText(getBaseContext(),"wrong wrong..",Toast.LENGTH_SHORT).show();
            }
        }
    }
}






 /*private class MeanTask extends AsyncTask<Void,Void,String>{

        int y = 1;
        @Override
        protected String doInBackground(Void... params) {
            //int x = 1;

            while ( y <29) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("MeanData");
                query.whereEqualTo("xPos", "1");
                query.whereEqualTo("yPos", String.valueOf(y));
                query.setLimit(4);
                query.findInBackground(new FindCallback<ParseObject>() {

                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            Log.d("score", "Retrieved " + scoreList.size() + " scores");

                            //for rows for each direction representing a single location
                            for (int i = 0; i < 4; i++) {
                                //collect data
                                String xPos = scoreList.get(i).getString("xPos");
                                String yPos = scoreList.get(i).getString("yPos");
                                String direction = scoreList.get(i).getString("direction");
                                String dlink7d28 = scoreList.get(i).getString("dlink7D28");
                                String dlink95A8 = scoreList.get(i).getString("dlink95A8");
                                String dlink7d8c = scoreList.get(i).getString("dlink7D8C");

                                DataPoint dataPoint = new DataPoint();
                                dataPoint.xPos = "1";
                                dataPoint.yPos = String.valueOf(y);
                                dataPoint.direction = direction;
                                dataPoint.dlink7D28 = dlink7d28;
                                dataPoint.dlink7D8C = dlink7d8c;
                                dataPoint.dlink95A8 = dlink95A8;
                                dataPoints.add(dataPoint);


                            }
                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });
                y++;
     //           break;
            }
            return String.valueOf(dataPoints.size());
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(), s.toString(), Toast.LENGTH_SHORT).show();
        }
    }
*/

