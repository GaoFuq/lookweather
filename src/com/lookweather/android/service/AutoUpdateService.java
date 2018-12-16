package com.lookweather.android.service;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.lookweather.android.gson.Weather;
import com.lookweather.android.util.HttpUtil;
import com.lookweather.android.util.Utility;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		updateWeather();
		updateBingPic();
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = 8 * 60 * 60 * 1000;// 8小时的毫秒数
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent in = new Intent(this, AutoUpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, in, 0);
		manager.cancel(pendingIntent);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
				pendingIntent);
		return super.onStartCommand(intent, flags, startId);
	}

	/*
	 * 更新必应每日一图
	 */
	private void updateBingPic() {
		// TODO Auto-generated method stub
		String requestBingPic = "http://guolin.tech/api/bing_pic";
		HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {

			@Override
			public void onResponse(Call call, Response response)
					throws IOException {
				// TODO Auto-generated method stub
				String bingPic = response.body().string();
				SharedPreferences.Editor editor = PreferenceManager
						.getDefaultSharedPreferences(AutoUpdateService.this)
						.edit();
				editor.putString("bingPic", bingPic);
				editor.apply();
			}

			@Override
			public void onFailure(Call arg0, IOException arg1) {
				// TODO Auto-generated method stub
				arg1.printStackTrace();
			}
		});

	}

	/*
	 * 更新天气信息
	 */
	private void updateWeather() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String weatherString = preferences.getString("weather", null);
		if (weatherString != null) {
			
			Weather weather = Utility.handleWeatherResponse(weatherString);
			String weatherId = weather.basic.weatherId;
			String weatherUrl = "http://guolin.tech/api/weather?cityid="
					+ weatherId + "&key=76738de3c18241459c3e1a85e6ce975d";
			HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

				@Override
				public void onResponse(Call call, Response response)
						throws IOException {
					// TODO Auto-generated method stub
					String responseText = response.body().string();
					Weather weather = Utility
							.handleWeatherResponse(responseText);
					if ((weather != null) && ("ok".equals(weather.status))) {
						SharedPreferences.Editor editor = PreferenceManager
								.getDefaultSharedPreferences(
										AutoUpdateService.this).edit();
						editor.putString("weather", responseText);
						editor.apply();
					}
				}

				@Override
				public void onFailure(Call arg0, IOException arg1) {
					// TODO Auto-generated method stub
					arg1.printStackTrace();
				}
			});
		}
	}

}
