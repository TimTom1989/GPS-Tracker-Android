package com.emre.gps;

import android.app.*;
import android.content.*;
import android.os.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		
		Intent i = new Intent(MainActivity.this, LocationService.class);
        MainActivity.this.startService(i);
		
		
		
		
		
    }
}
