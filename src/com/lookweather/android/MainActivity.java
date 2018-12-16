package com.lookweather.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		/*
		 * 当有缓存时，让应用一打开就显示缓存的天气信息
		 */
		if ((preferences.getString("weather", null)) != null) {
			startActivity(new Intent(this, WeatherActivity.class));
			finish();
		}

	}
}
