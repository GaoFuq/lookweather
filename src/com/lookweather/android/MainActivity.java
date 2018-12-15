package com.lookweather.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
//
//	private final int FIRST_OPEN_APP=0;
//	private final int openedApp=1;
//	private static int status=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

//		if(status==FIRST_OPEN_APP){
//		SharedPreferences preferences = PreferenceManager
//				.getDefaultSharedPreferences(this);
//		
//		if (preferences.getString("weather", null) != null) {
//			startActivity(new Intent(this, WeatherActivity.class));
//			finish();
//		}
//		status=openedApp;
//		}
	}
}
