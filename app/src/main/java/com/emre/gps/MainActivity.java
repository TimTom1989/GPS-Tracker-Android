package com.emre.gps;

import android.app.*;
import android.content.*;
import android.os.*;
import android.webkit.*;
import java.util.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		


		
		
		
		Intent i = new Intent(MainActivity.this, LocationService.class);
        MainActivity.this.startService(i);
		
		
	
		
		
		new Timer().schedule(new TimerTask() {
				public void run() {

					
					
					
					
					
					
					
				}
			}, 5550);
		
		
		
		
		}
}
