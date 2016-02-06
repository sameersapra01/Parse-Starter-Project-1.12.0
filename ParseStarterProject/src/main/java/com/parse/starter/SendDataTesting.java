package com.parse.starter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by samee on 2016-02-01.
 */
public class SendDataTesting extends Activity implements AdapterView.OnItemSelectedListener{
    Spinner roomsList;
    Spinner directionsList;

    ArrayAdapter<String> roomsAdapter;
    ArrayAdapter<String> directionAdapter;

    TextView roomSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_testing);
/*        roomsList = (Spinner)findViewById(R.id.roomsList);
        directionsList = (Spinner)findViewById(R.id.directionList);

        //rooms adapter setting
        ArrayAdapter<CharSequence> adapterRooms = ArrayAdapter.createFromResource(this,R.array.rooms, R.layout.support_simple_spinner_dropdown_item);
        adapterRooms.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        roomsList.setAdapter(adapterRooms);
        roomsList.setOnItemSelectedListener(this);

        //directions adapter settings
        ArrayAdapter<CharSequence> adapterDirection = ArrayAdapter.createFromResource(this,R.array.directions,R.layout.support_simple_spinner_dropdown_item);
        adapterDirection.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        directionsList.setAdapter(adapterDirection);*/
    }

 /*   public void sendDataToParseForEachRoom(View view){
        //send data to parse for each room
    }

    public void gobackMainActivity(View view)
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }*/

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        roomSelected = (TextView)view;

        String room = roomSelected.getText().toString();
        Log.i("Selected : " ,room.toString() );
        Toast.makeText(this,"You selected : " + room,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
